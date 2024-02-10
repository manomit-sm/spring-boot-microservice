package com.bsolz.util.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class ServiceUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceUtil.class);


    private final String port;

    private String serviceAddress;

    public ServiceUtil(@Value("${server.port}") String port) {
        this.port = port;
        this.serviceAddress = null;
    }

    public String getServiceAddress() {
        if (serviceAddress == null) {
            serviceAddress = findMyHostname() + "/" + findMyAddress() +  ":" + port;
        }
        return serviceAddress;
    }

    private String findMyAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            return "Unknown ip address";
        }
    }

    private String findMyHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            return "Unknown host exception";
        }
    }


}
