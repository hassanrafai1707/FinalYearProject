//package com.FinalYearProject.FinalYearProject;
//
//
//
//import org.springframework.boot.ConfigurableBootstrapContext;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.SpringApplicationRunListener;
//import org.springframework.core.env.MapPropertySource;
//import org.springframework.core.env.ConfigurableEnvironment;
//import org.springframework.web.client.RestTemplate;
//
//import java.net.Inet4Address;
//import java.net.InetAddress;
//import java.net.NetworkInterface;
//import java.util.Enumeration;
//import java.util.HashMap;
//import java.util.Map;
//
////This Class runs before the app starts by using SpringApplicationRunListener
////prerequisites need to make a file with .factories in resource
////in the .factories file must specify the path of the class implementing SpringApplicationRunListener
//public class DynamicEnvironmentUpdater implements SpringApplicationRunListener {
//    public DynamicEnvironmentUpdater (SpringApplication application,String[] args){};
//
//    @Override
//    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext
//                                    ,ConfigurableEnvironment environment){
//        setIPForSever(environment);
//    }
//    //this function is user to set the server.address before tomcat runs so tomcat can use it
//    public void setIPForSever(ConfigurableEnvironment environment){
//        RestTemplate restTemplate = new RestTemplate();
//        String PublicIP="0.0.0.0.";//belongs to your router, not your laptop
//        String LocalIP="127.0.0.1";//your laptop in the network
//        try {
//            PublicIP=restTemplate.getForObject("https://api.ipify.org?format=text", String.class);
//            LocalIP= getRealLocalIp();
//            System.out.println("\n\nPublic IP ="+PublicIP);
//            System.out.println("\n\nLocal IP ="+LocalIP);
//        }
//        catch (Exception e){
//            System.err.println("\nFailed to get public IP:"  + e.getMessage());
//        }
//        Map<String,Object> props= new HashMap<>();
//        props.put("dynamic.public.ip",PublicIP);
//        props.put("dynamic.local.ip",LocalIP);
//        props.put("server.address",LocalIP);
//        environment.getPropertySources().addFirst(new MapPropertySource("DynamicIP",props));
//    }
//    //this function goes through all the different Ip the server has and then selects the IPV4 IP address
//    public String getRealLocalIp(){
//        try {
//            Enumeration<NetworkInterface> inetAddressEnumeration= NetworkInterface.getNetworkInterfaces();
//            while (inetAddressEnumeration.hasMoreElements()){
//                NetworkInterface networkInterface=inetAddressEnumeration.nextElement();
//                if (
//                        !networkInterface.isUp() ||
//                        networkInterface.isLoopback() ||
//                        networkInterface.getDisplayName().contains("VirtualBox")
//                )continue;
//                        Enumeration<InetAddress> address=networkInterface.getInetAddresses();
//                        while (address.hasMoreElements()){
//                            InetAddress addr = address.nextElement();
//                            if(addr instanceof Inet4Address){
//                                return addr.getHostAddress();
//                            }
//                        }
//                }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return"127.0.0.1";
//    }
//}
