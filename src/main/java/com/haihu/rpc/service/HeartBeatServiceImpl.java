package com.haihu.rpc.service;

import org.springframework.stereotype.Repository;
import com.haihu.rpc.common.RemoteService;

@Repository
@RemoteService
public class HeartBeatServiceImpl implements HeartBeatService {
	public byte getHeartBeat(String version){
		return (byte)1;
	}
}
