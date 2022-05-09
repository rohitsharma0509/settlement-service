package com.scb.settlement.config;

import org.apache.kafka.clients.consumer.CooperativeStickyAssignor;

import java.util.Collections;
import java.util.List;

public class CustomCooperativeStickyAssignor extends CooperativeStickyAssignor {

    @Override
    public List<RebalanceProtocol> supportedProtocols() {
        return Collections.singletonList(RebalanceProtocol.COOPERATIVE);
    }

}
