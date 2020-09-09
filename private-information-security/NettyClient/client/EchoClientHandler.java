package com.hyun.client;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class EchoClientHandler extends ChannelInboundHandlerAdapter {
	static int num = 0;

	// 소켓 채널이 최초 활성화 되었을때 실행
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		String sendMessage = "Hello, Netty!";

		ByteBuf messageBuffer = Unpooled.buffer();
		messageBuffer.writeBytes(sendMessage.getBytes());

		StringBuilder builder = new StringBuilder();
		builder.append("전송한 문자열 [");
		builder.append(sendMessage);
		builder.append("]");

		System.out.println(builder.toString());
		ctx.writeAndFlush(messageBuffer);
	}

	// 서버로부터 수신된 데이터가 있을 때 호출되는 메서드
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {

		// 서버로 부터 수신된 데이터가 저장된 msg 객체에서 문자열 데이터 추출
		String readMessage = ((ByteBuf) msg).toString(Charset.defaultCharset());

		StringBuilder builder = new StringBuilder();
		builder.append("서버로부터 수신한 문자열 [");
		builder.append(readMessage);
		builder.append("]");

		System.out.println(builder.toString());

	}

	public void receive(ChannelHandlerContext ctx) throws InterruptedException {

		try {
			String nums;
			nums = Integer.toString(num);
			Thread.sleep(5000);
			String sendMessage = "codereview" + nums;

			if (num > 0) {
				ByteBuf messageBuffer = Unpooled.buffer();
				messageBuffer.writeBytes(sendMessage.getBytes());

				StringBuilder builder = new StringBuilder();
				builder.append("전송한 문자열 [");
				builder.append(sendMessage);
				builder.append("]");

				System.out.println(builder.toString());
				ctx.writeAndFlush(messageBuffer);
			}
			num += 1;

		} catch (Exception e) {
		} finally {
			Thread.interrupted();
		}
	}

	// 수신된 데이터를 모두 읽었을때 호출되는 이벤트 메서드
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws InterruptedException {

		receive(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

		cause.printStackTrace();
		ctx.close();
	}

}
