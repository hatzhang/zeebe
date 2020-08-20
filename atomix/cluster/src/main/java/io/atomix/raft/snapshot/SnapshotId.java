/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.atomix.raft.snapshot;

import io.atomix.utils.time.WallClockTimestamp;
import java.util.Comparator;

/** Represents an identifier of an {@link PersistedSnapshot}. */
public interface SnapshotId extends Comparable<SnapshotId> {

  /** @return the index of the snapshot */
  long getIndex();

  /** @return the term when the snapshot was taken */
  long getTerm();

  long getProcessedPosition();

  /** @return the timestamp when the snapshot was taken */
  WallClockTimestamp getTimestamp();

  /**
   * The string representation of the snapshot identifier, looks like 'index-term-timestamp'.
   *
   * @return the string representation
   */
  String getSnapshotIdAsString();

  /**
   * A snapshot is considered "lower" if its term is less than that of the other snapshot. If they
   * are the same, then it is considered "lower" if its index is less then the other, if they are
   * equal then the timestamps are compared. If they are the same then these snapshots are the same
   * order-wise.
   *
   * @param other the snapshot to compare against
   * @return -1 if {@code this} is less than {@code other}, 0 if they are the same, 1 if it is
   *     greater
   */
  @Override
  default int compareTo(final SnapshotId other) {
    return Comparator.comparingLong(SnapshotId::getTerm)
        .thenComparing(SnapshotId::getIndex)
        .thenComparing(SnapshotId::getProcessedPosition)
        .thenComparing(SnapshotId::getTimestamp)
        .compare(this, other);
  }
}
