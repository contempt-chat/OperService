package com.ircnet.service.operserv.web;

import com.ircnet.service.operserv.Util;
import com.ircnet.service.operserv.irc.IRCUser;
import com.ircnet.service.operserv.kline.KLine;
import com.ircnet.service.operserv.kline.KLineDTO;
import com.ircnet.service.operserv.kline.KLineMapper;
import com.ircnet.service.operserv.kline.KLineService;
import com.ircnet.service.operserv.match.MatchService;
import com.ircnet.service.operserv.web.dto.WhoDTO;
import com.ircnet.service.operserv.web.dto.WhoReplyDTO;
import com.ircnet.service.operserv.web.dto.WhoUserDTO;
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
    LOGGER.debug("Received {}", klineDTO);
    KLine kline = KLineMapper.map(klineDTO);
    klineService.create(null, kline, klineDTO.getDuration(), false);

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
    klineService.loadFromAPI(null);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @RequestMapping(value = "/who", method = RequestMethod.POST)
  public ResponseEntity<Object> who(@RequestBody WhoDTO whoDTO) {
    boolean isIpAddressOrRange = Util.isIpAddressOrRange(whoDTO.getHostname());
    List<IRCUser> matchingUsers = matchService.findMatching(whoDTO.getUsername(), whoDTO.getHostname(),
        isIpAddressOrRange, whoDTO.getSid(), null);
    List<WhoUserDTO> whoUserDTOs = matchingUsers.stream().map(e -> mapIRCUser(e)).collect(Collectors.toList());
    WhoReplyDTO whoReplyDTO = new WhoReplyDTO();
    whoReplyDTO.setUsers(whoUserDTOs);
    whoReplyDTO.setTotal(whoUserDTOs.size());
    return ResponseEntity.status(HttpStatus.OK).body(whoReplyDTO);
  }

  private WhoUserDTO mapIRCUser(IRCUser ircUser) {
    WhoUserDTO whoUserDTO = new WhoUserDTO();
    whoUserDTO.setSid(ircUser.getSid());
    whoUserDTO.setNick(ircUser.getNick());
    whoUserDTO.setUsername(ircUser.getUser());
    whoUserDTO.setHost(ircUser.getHost());
    whoUserDTO.setIpAddress(ircUser.getIpAddress());
    whoUserDTO.setRealName(ircUser.getRealName());
    whoUserDTO.setAccount(ircUser.getAccount());
    return whoUserDTO;
  }
}