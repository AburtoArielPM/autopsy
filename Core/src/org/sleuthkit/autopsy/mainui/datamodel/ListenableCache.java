/*
 * Autopsy Forensic Browser
 *
 * Copyright 2021 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.mainui.datamodel;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sleuthkit.autopsy.coreutils.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitResult;
import reactor.util.concurrent.Queues;

/**
 *
 */
public abstract class ListenableCache<D, K, V> {

    private static final Logger logger = Logger.getLogger(ListenableCache.class.getName());

    private final Cache<K, V> cache = CacheBuilder.newBuilder().maximumSize(1000).build();

    // taken from https://stackoverflow.com/questions/66671636/why-is-sinks-many-multicast-onbackpressurebuffer-completing-after-one-of-t
    private final Sinks.Many<K> invalidatedKeyMulticast = Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

    public V getValue(K key) throws IllegalArgumentException, ExecutionException {
        validateCacheKey(key);
        return cache.get(key, () -> fetch(key));
    }

    private V getValueLoggedError(K key) {
        try {
            return getValue(key);
        } catch (IllegalArgumentException | ExecutionException ex) {
            logger.log(Level.WARNING, "An error occurred while fetching results for key: " + key, ex);
            return null;
        }
    }

    public Flux<V> getInitialAndUpdates(K key) throws IllegalArgumentException {
        validateCacheKey(key);
        
        // GVDTODO handle in one transaction
        Flux<V> initial = Flux.fromStream(Stream.of(getValueLoggedError(key)));

        Flux<V> updates = this.invalidatedKeyMulticast.asFlux()
                .filter(invalidatedKey -> key.equals(invalidatedKey))
                .map((matchingInvalidatedKey) -> getValueLoggedError(matchingInvalidatedKey));

        return Flux.concat(initial, updates)
                .filter((data) -> data != null);
    }

    public void invalidateAll() {
        List<K> keys = new ArrayList<>(cache.asMap().keySet());
        invalidateAndBroadcast(keys);
    }

    public void invalidate(D eventData) {
        List<K> keys = cache.asMap().keySet().stream()
                .filter((key) -> matches(eventData, key))
                .collect(Collectors.toList());
        invalidateAndBroadcast(keys);
    }

    private void invalidateAndBroadcast(Iterable<K> keys) {
        cache.invalidateAll(keys);
        keys.forEach((k) -> {
            EmitResult emitResult = invalidatedKeyMulticast.tryEmitNext(k);
            if (emitResult.isFailure()) {
                logger.log(Level.WARNING, MessageFormat.format("There was an error broadcasting invalidated key {0}: {1}", k, emitResult.name()));
            }
        });
    }

    protected void validateCacheKey(K key) throws IllegalArgumentException {
        // to be overridden
        if (key == null) {
            throw new IllegalArgumentException("Expected non-null key");
        }
    }

    protected abstract V fetch(K key) throws Exception;

    protected abstract boolean matches(D eventData, K key);
}
