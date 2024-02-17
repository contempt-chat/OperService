package com.ircnet.service.operserv.irc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

  @Autowired
  @Qualifier("userMapByUID")
  private Map<String, IRCUser>  userMapByUID;

  @Autowired
  @Qualifier("userMapByNick")
  private Map<String, IRCUser> userMapByNick;

  @Override
  public void add(IRCUser user) {
    userMapByUID.put(user.getUid(), user);
    userMapByNick.put(user.getNick(), user);
  }

  @Override
  public IRCUser findByUID(String uid) {
    return userMapByUID.get(uid);
  }

  @Override
  public IRCUser findByNick(String nick) {
    return userMapByNick.get(nick);
  }

  @Override
  public IRCUser findByUIDorNick(String arg) {
    // Try to find by UID
    IRCUser user = userMapByUID.get(arg);

    if(user != null) {
      return user;
    }

    // Try to find by nick
    user = userMapByNick.get(arg);

    if(user != null) {
      return user;
    }

    LOGGER.warn("Could not find user by uid '{}' or nick '{}'", arg, arg);
    return null;
  }

  @Override
  public void rename(IRCUser user, String newNick) {
    userMapByNick.remove(user.getNick());
    user.setNick(newNick);
    userMapByNick.put(newNick, user);
    // We do not have to touch userMapByUID because it is the same object
  }

  @Override
  public void remove(IRCUser user) {
    IRCUser userInMap;

    userInMap = userMapByUID.remove(user.getUid());
    if(userInMap == null) {
      LOGGER.error("userMapByUID.remove(\"{}\") failed", user.getUid());
    }

    userInMap = userMapByNick.remove(user.getNick());
    if(userInMap == null) {
      LOGGER.error("userMapByNick.remove(\"{}\") failed", user.getNick());
    }
  }
}
