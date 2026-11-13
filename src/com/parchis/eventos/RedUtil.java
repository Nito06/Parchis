package com.parchis.eventos;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Utilidad de red para detectar y formatear las direcciones IP locales y virtuales
 * (Wi-Fi, Ethernet, Tailscale, ZeroTier).
 */
public class RedUtil {

    public static class InfoIP {
        private final String interfaz;
        private final String ip;
        private final String tipo;

        public InfoIP(String interfaz, String ip, String tipo) {
            this.interfaz = interfaz;
            this.ip = ip;
            this.tipo = tipo;
        }

        public String getInterfaz() {
            return interfaz;
        }

        public String getIp() {
            return ip;
        }

        public String getTipo() {
            return tipo;
        }

        @Override
        public String toString() {
            return tipo + ": " + ip;
        }
    }

    /**
     * Retorna la lista de IPs IPv4 activas en la máquina clasificadas por tipo.
     */
    public static List<InfoIP> obtenerDireccionesIP() {
        List<InfoIP> lista = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) {
                        String ip = addr.getHostAddress();
                        String name = iface.getName().toLowerCase();
                        String tipo = "Red Local (LAN/Wi-Fi)";

                        if (name.contains("tailscale") || ip.startsWith("100.")) {
                            tipo = "Tailscale VPN";
                        } else if (name.contains("zt") || name.contains("zerotier") || ip.startsWith("10.")) {
                            if (name.contains("zt") || name.contains("zerotier")) {
                                tipo = "ZeroTier VPN";
                            }
                        } else if (name.startsWith("en") || name.startsWith("wlan") || name.startsWith("eth")) {
                            tipo = "Wi-Fi / Ethernet";
                        }

                        lista.add(new InfoIP(iface.getName(), ip, tipo));
                    }
                }
            }
        } catch (Exception ignored) {}

        if (lista.isEmpty()) {
            lista.add(new InfoIP("lo0", "127.0.0.1", "Localhost (Misma PC)"));
        }
        return lista;
    }

    /**
     * Retorna la IP más recomendada para compartir con un amigo en la red.
     */
    public static String getIPRecomendada() {
        List<InfoIP> ips = obtenerDireccionesIP();
        // Preferir Tailscale si está activo
        for (InfoIP info : ips) {
            if (info.getTipo().contains("Tailscale")) return info.getIp();
        }
        // O ZeroTier
        for (InfoIP info : ips) {
            if (info.getTipo().contains("ZeroTier")) return info.getIp();
        }
        // O Wi-Fi / Ethernet
        for (InfoIP info : ips) {
            if (info.getTipo().contains("Wi-Fi") || info.getIp().startsWith("192.168.")) return info.getIp();
        }
        return ips.get(0).getIp();
    }
}
