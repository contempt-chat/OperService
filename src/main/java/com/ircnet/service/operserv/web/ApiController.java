package com.ircnet.service.operserv.web;

import com.ircnet.service.operserv.Util;
import com.ircnet.service.operserv.irc.IRCUser;
import com.ircnet.service.operserv.kline.KLine;
import com.ircnet.service.operserv.kline.KLineDTO;
import com.ircnet.service.operserv.kline.KLineMapper;
import com.ircnet.service.operserv.kline.KLineService;
import com.ircnet.service.operserv.match.MatchService;
import com.ircnet.service.operserv.web.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ApiController {
  private static final Logger LOGGER = LoggerFactory.getLogger(ApiController.class);

  @Autowired
  private KLineService klineService;

  @Autowired
  private MatchService matchService;

  @RequestMapping(value = "/k-line", method = RequestMethod.POST)
  public ResponseEntity<Object> addKLine(@RequestBody KLineDTO klineDTO) {
    LOGGER.debug("Received K-Line: {}", klineDTO);
    KLine kline = KLineMapper.map(klineDTO);
    klineService.create(null, kline, klineDTO.getDuration());

    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @RequestMapping(value = "/k-line/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<Object> deleteKLine(@PathVariable long id) {
    KLine kline = klineService.find(id);

    if (kline == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    klineService.removeKLine(kline, null);
    LOGGER.info("Removed K-Line {}", kline);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @RequestMapping(value = "/k-line/reload", method = RequestMethod.POST)
  public ResponseEntity<Object> reloadKLines() {
    LOGGER.debug("Refetching K-Lines");
    try {
      klineService.loadFromAPI();
      return ResponseEntity.status(HttpStatus.OK).build();
    }
    catch (Exception e) {
      LOGGER.warn("Could not reload K-Lines", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorDTO(e.getMessage()));
    }
  }

  @RequestMapping(value = "/who", method = RequestMethod.POST)
  public ResponseEntity<WhoReplyDTO> who(@RequestBody WhoDTO whoDTO) {
    LOGGER.debug("Received WHO: {}", whoDTO);
    boolean isIpAddressOrRange = Util.isIpAddressOrRange(whoDTO.getHostname());
    List<IRCUser> matchingUsers = matchService.findMatching(whoDTO.getUsername(), whoDTO.getHostname(),
        isIpAddressOrRange, whoDTO.getSid(), whoDTO.getAccount(), whoDTO.isExcludeUsersWithIdent());
    List<WhoUserDTO> whoUserDTOs = matchingUsers.stream().map(e -> mapIRCUser(e)).collect(Collectors.toList());
    WhoReplyDTO whoReplyDTO = new WhoReplyDTO();
    whoReplyDTO.setUsers(whoUserDTOs);
    whoReplyDTO.setTotal(whoUserDTOs.size());
    return ResponseEntity.status(HttpStatus.OK).body(whoReplyDTO);
  }

  @RequestMapping(value = "/integrity-check", method = RequestMethod.POST)
  public ResponseEntity<IntegrityCheckDTO> integrityCheck() {
    IntegrityCheckDTO integrityCheckDTO = new IntegrityCheckDTO();
    integrityCheckDTO.setKlineChecksum(klineService.createCheckSum());
    return ResponseEntity.status(HttpStatus.OK).body(integrityCheckDTO);
  }

  private WhoUserDTO mapIRCUser(IRCUser ircUser) {
    WhoUserDTO whoUserDTO = new WhoUserDTO();
    whoUserDTO.setSid(ircUser.getServer().getSid());
    whoUserDTO.setServerName(ircUser.getServer().getName());
    whoUserDTO.setUid(ircUser.getUid());
    whoUserDTO.setNick(ircUser.getNick());
    whoUserDTO.setUsername(ircUser.getUser());
    whoUserDTO.setHost(ircUser.getHost());
    whoUserDTO.setIpAddress(ircUser.getIpAddress());
    whoUserDTO.setRealName(ircUser.getRealName());
    whoUserDTO.setAccount(ircUser.getAccount());
    return whoUserDTO;
  }
}
