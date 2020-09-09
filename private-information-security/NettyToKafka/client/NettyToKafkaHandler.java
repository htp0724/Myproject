package com.hyun.client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import com.hyun.client.kafka.Producer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

// 입력된 데이터를 처리하는 이벤트 핸들러 상속
public class NettyToKafkaHandler extends ChannelInboundHandlerAdapter {

	// producer 객체 생성
	static Producer producer = new Producer();
	
	int count = 0;

	// 데이터 수신 이벤트 처리 메서드. 클라이언트로부터 데이터의 수신이 이루어졌을때 네티가 자동으로 호출하는 이벤트 메소드
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws UnknownHostException, IOException {

		// 수신된 데이터를 가지고 있는 네티의 바이트 버퍼 객체로 부터 문자열 객체를 읽어온다.
		String readMessage = ((ByteBuf) msg).toString(Charset.defaultCharset());
		System.out.println("수신한 문자열 [" + readMessage + "]");

		// ctx는 채널 파이프라인에 대한 이벤트를 처리한다. 여기서는 서버에 연결된 클라이언트 채널로 입력받은 데이터를 그대로 전송한다.
		// NettyServer에게 데이터를 주는 채널 파이프라인이 연결된 NettyClient에게 전송
		ctx.write(msg);

		if (count >= 1) {
			
			// 클라이언트에게 받은 메시지를 producer가 kafka에 전송
			producer.sendmsgs(readMessage);
			System.out.println("Kafka전송" + readMessage);
			
			}
		count += 1;
	}

	// channelRead 이벤트의 처리가 완료된 후 자동으로 수행되는 이벤트 메서드
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {

		// 채널 파이프 라인에 저장된 버퍼를 전송
		ctx.flush();

	}

	// 채널 활성화 시 실행되는 메소드 (제일 먼저 실행)
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws UnknownHostException, IOException {
		producer.set();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		ctx.close();
	}
}
