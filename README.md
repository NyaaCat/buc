# buc
BungeeCord User Control - Whitelisting / Banning for BungeeCord.

# HAProxy Support

Set `haproxy` to `true` and add all your HAProxy server IP to the list. Only IPs in this list will enable PROXY protocol support, traffic from other IPs is treated as normal TCP traffic.

Example of HAProxy configuration file:

```
global
        ulimit-n  4096

defaults
        log global
        mode    tcp
        option  dontlognull
        timeout connect 1000
        timeout client 150000
        timeout server 150000

frontend mc-in
        bind *:25565
        default_backend mc-out

backend mc-out
        server mcbackend1 backend1:25565 send-proxy-v2
        server mcbackend2 backend2:25565 send-proxy-v2
        source 1.2.3.4 # your haproxy public ip address
```
