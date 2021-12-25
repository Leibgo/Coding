package chapter2;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

public class InetAddressExample {
    //NetworkInterface:网络接口类(Wifi,以太网)
    //InetAddress:IP地址类
    public static void main(String[] args) {
        //获取互联网接口以及主机的连接地址
        try {
            //1.NetWorkInterface.getNetworkInterfaces()可以获取主机所有网络地址(网络接口)，并对应到NetworkInterface实例
            Enumeration<NetworkInterface> interfaceList = NetworkInterface.getNetworkInterfaces();
            if(interfaceList == null){
                System.out.println("--No Interfaces Found--");
            }else{
                while(interfaceList.hasMoreElements()){
                    NetworkInterface iface = interfaceList.nextElement();
                    System.out.println("Interface " + iface.getName() + ":");
                    //2.获取地址
                    Enumeration<InetAddress> addrList = iface.getInetAddresses();
                    if(!addrList.hasMoreElements()){
                        System.out.println("No Address For This Interface");
                    }
                    while(addrList.hasMoreElements()){
                        InetAddress address = addrList.nextElement();
                        System.out.println("Address" + (address instanceof Inet4Address ? "(V4)" : (address instanceof Inet6Address ? "(V6)" : "(?)")));
                        System.out.println(":" + address.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            System.out.println("Error Getting Network Interfaces:" + e.getMessage());
        }
        System.out.println("=========================================");
        //通过命令行输入的主机名或者ip地址获取该主机的所有IP地址
        for(String host : args){
            System.out.println(host + ":");
            try {
                InetAddress[] addressList = InetAddress.getAllByName(host);
                for (InetAddress address : addressList) {
                    System.out.println(address.getHostName() + "/" + address.getHostAddress());
                }
            } catch (UnknownHostException e) {
                System.out.println("Unable to find address for" + host);
            }
        }
    }
}
