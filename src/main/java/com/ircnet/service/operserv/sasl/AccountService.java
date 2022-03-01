package com.ircnet.service.operserv.sasl;

import com.ircnet.library.common.User;

import java.util.List;

// Checked (FIXME)
public interface AccountService {
  /**
   * Checks if a SASL account is authorized to use this service.
   *
   * @param account A SASL account
   * @return True if the account is allowed to use this service
   */
  boolean isAuthorized(String account);

  /**
   * Does an API call to load SASL accounts which are authorized to use this service.
   *
   * @param from An IRC user, if this method was executed via SQUERY otherwise null
   */
  void loadFromAPI(User from);

  /**
   * Returns a list of SASL accounts which are authorized to use this service.
   */
  List<String> findAll();
}
