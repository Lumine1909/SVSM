package io.github.lumine1909.svsm.server;

import ca.spottedleaf.concurrentutil.collection.MultiThreadedQueue;
import io.github.lumine1909.reflexion.Field;
import io.github.lumine1909.svsm.util.DummyQueue;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.papermc.paper.util.KeepAlive;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.util.Util;
import net.minecraft.network.VarInt;
import net.minecraft.network.protocol.common.CommonPacketTypes;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import java.util.UUID;

import static io.github.lumine1909.svsm.SVSMPlugin.plugin;

public class Player {

    private static final long KEEP_ALIVE_PERIOD = 5000;
    private static final int KEEP_ALIVE_IN;
    private static final int KEEP_ALIVE_OUT;

    private static final Field<KeepAlive> field$keepAlive = Field.of(ServerCommonPacketListenerImpl.class, "keepAlive");
    @SuppressWarnings("rawtypes")
    private static final Field<MultiThreadedQueue> field$pendingKeepAlives = Field.of(KeepAlive.class, "pendingKeepAlives");

    static {
        int[] id = {0, 0};
        GameProtocols.CLIENTBOUND_TEMPLATE.details().listPackets((packetType, i) -> {
            if (packetType.equals(CommonPacketTypes.CLIENTBOUND_KEEP_ALIVE)) {
                id[0] = i;
            }
        });
        KEEP_ALIVE_OUT = id[0];
        GameProtocols.SERVERBOUND_TEMPLATE.details().listPackets((packetType, i) -> {
            if (packetType.equals(CommonPacketTypes.SERVERBOUND_KEEP_ALIVE)) {
                id[1] = i;
            }
        });
        KEEP_ALIVE_IN = id[1];
    }

    private PlayerInfo info;
    private volatile long prevKeepAlive = Util.getMillis();

    private Player() {
    }

    public static Player createFromBukkit(org.bukkit.entity.Player bukkitPlayer) {
        ServerPlayer sp = ((CraftPlayer) bukkitPlayer).getHandle();
        field$pendingKeepAlives.set(field$keepAlive.get(sp.connection), DummyQueue.INSTANCE);
        Player player = new Player();
        Channel channel = sp.connection.connection.channel;
        if (channel.pipeline().get("svsm_inbound_handler") != null) {
            channel.pipeline().remove("svsm_inbound_handler");
        }
        if (channel.pipeline().get("svsm_outbound_handler") != null) {
            channel.pipeline().remove("svsm_outgbound_handler");
        }
        channel.pipeline().replace("timeout", "timeout", new ReadTimeoutHandler(Integer.MAX_VALUE));
        channel.pipeline().addBefore("decoder", "svsm_inbound_handler", new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                if (msg instanceof ByteBuf buf && buf.isReadable()) {
                    if (VarInt.read(buf) == KEEP_ALIVE_IN) {
                        return;
                    }
                    buf.readerIndex(0);
                }
                super.channelRead(ctx, msg);
            }
        });
        channel.pipeline().addBefore("encoder", "svsm_outbound_handler", new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                if (msg instanceof ByteBuf buf && buf.isReadable()) {
                    if (VarInt.read(buf) == KEEP_ALIVE_OUT) {
                        player.prevKeepAlive = buf.readLong();
                    }
                    buf.readerIndex(0);
                }
                super.write(ctx, msg, promise);
            }
        });
        player.info = new PlayerInfo(sp.getScoreboardName(), sp.getUUID(), channel, channel.pipeline().context("svsm_outbound_handler"));
        VirtualServer.SERVER.playerConnect(player);
        channel.closeFuture().addListener(f -> player.handleDisconnect());
        Bukkit.getScheduler().runTaskLater(plugin, () -> System.out.println(channel.pipeline()), 1);
        return player;
    }

    public PlayerInfo info() {
        return info;
    }

    public void keepAlive() {
        long curr = Util.getMillis();
        if (curr - prevKeepAlive > KEEP_ALIVE_PERIOD) {
            sendKeepAlivePacket();
            prevKeepAlive = Util.getMillis();
        }
    }

    private void handleDisconnect() {
        VirtualServer.SERVER.playerDisconnect(this);
    }

    private void sendKeepAlivePacket() {
        ByteBuf buf = Unpooled.buffer();
        VarInt.write(buf, KEEP_ALIVE_OUT);
        buf.writeLong(Util.getMillis());
        info.ctx.writeAndFlush(buf);
    }

    public record PlayerInfo(String name, UUID uuid, Channel channel, ChannelHandlerContext ctx) {

    }
}