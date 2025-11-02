package com.FinalYearProject.FinalYearProject.Config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.web.client.RestTemplate;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;


//this class is used to dynamically set the PublicIP can be useful in the futures
@Configuration
public class IpConfig {
    private String PublicIP;
    private String LocalIP;
    @EventListener(ApplicationReadyEvent.class)
    public void getIpAtStart(){
        RestTemplate restTemplate = new RestTemplate();
        try {
            this.PublicIP=restTemplate.getForObject("https://api.ipify.org?format=text", String.class);
            System.out.println("\n\nPublic IP ="+PublicIP);
            Enumeration<NetworkInterface> inetAddressEnumeration= NetworkInterface.getNetworkInterfaces();
            while (inetAddressEnumeration.hasMoreElements()){
                NetworkInterface networkInterface=inetAddressEnumeration.nextElement();
                if (
                        !networkInterface.isUp() ||
                                networkInterface.isLoopback() ||
                                networkInterface.getDisplayName().contains("VirtualBox")
                )continue;
                Enumeration<InetAddress> address=networkInterface.getInetAddresses();
                while (address.hasMoreElements()){
                    InetAddress addr = address.nextElement();
                    if(addr instanceof Inet4Address){
                        LocalIP= addr.getHostAddress();
                    }
                }
            }
        }
        catch (Exception e){
            System.err.println
                    (
                            "\nFailed to get public IP:"  + e.getMessage()
                                    +"\n Failed to get Local IP:"+e.getMessage()
                    );
        }
    }
    //annotated for further use
    @Bean
    public String getPublicIP(){
        return PublicIP;
    }
    //annotated for further use
    @Bean
    public String getLocalIp(){
        return LocalIP;
    }
}
