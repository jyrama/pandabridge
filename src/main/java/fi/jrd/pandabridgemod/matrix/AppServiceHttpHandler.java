package fi.jrd.pandabridgemod.matrix;

import com.google.gson.Gson;

import fi.jrd.pandabridgemod.PandabridgeMod;
import fi.jrd.pandabridgemod.matrix.Transaction.RoomMessageEvent;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;

public class AppServiceHttpHandler extends SimpleChannelInboundHandler {
    private static final Gson gson = new Gson();

    private HttpRequest request;

    StringBuilder responseData = new StringBuilder();

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private void writeResponse(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE,
                Unpooled.EMPTY_BUFFER);
        ctx.write(response);
    }

    private void writeResponse(ChannelHandlerContext ctx, StringBuilder responseData) {
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.copiedBuffer(responseData.toString(), CharsetUtil.UTF_8));

        httpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");

        if (keepAlive) {
            httpResponse.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, httpResponse.content().readableBytes());
            httpResponse.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ctx.write(httpResponse);

        if (!keepAlive) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;
            if (HttpUtil.is100ContinueExpected(request)) {
                writeResponse(ctx);
            }
            responseData.setLength(0);

            if (request.uri().startsWith("/_matrix/app/v1/transactions/")) {
                PandabridgeMod.logger.debug("New transaction: {}", request.uri());
            } else if (request.uri().startsWith("/_matrix/app/v1/users/")) {
                PandabridgeMod.logger.debug("Users query: ", request.uri());
            } else if (request.uri().startsWith("/_matrix/app/v1/rooms/")) {
                PandabridgeMod.logger.debug("Rooms query:", request.uri());
            }

        }

        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;
            String strContent = httpContent.content().toString(CharsetUtil.UTF_8);
            PandabridgeMod.logger.debug("StrContent: {}", strContent);

            Transaction tx = gson.fromJson(strContent, Transaction.class);

            for (RoomMessageEvent message : tx.getAllTextMessages()) {
                PandabridgeMod.broadcast(message.sender(), message.body());
            }

            if (msg instanceof LastHttpContent) {
                responseData.append("{}");
                writeResponse(ctx, responseData);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
