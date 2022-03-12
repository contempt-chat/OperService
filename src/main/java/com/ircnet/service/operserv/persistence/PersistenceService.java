package com.ircnet.service.operserv.persistence;

/**
 * A service for saving and loading data (K-Lines, authorized SASL accounts).
 */
public interface PersistenceService {
  /**
   * Saves data (K-Lines, authorized SASL accounts) to a file.
   */
  void save();

  /**
   * Schedules next execute of save() after a configured delay.
   */
  void scheduleSave();

  /**
   * Loads data (K-Lines, authorized SASL accounts) from a file.
   */
  void load();
}
