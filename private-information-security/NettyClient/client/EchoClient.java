package com.hyun.client;

import java.util.Scanner;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

@SpringBootApplication
public class EchoClient {

	static final boolean SSL = System.getProperty("user.dir") + "/mykeystore.jks" != null;
	static String host;
	static int port;
	static Scanner sc = new Scanner(System.in);

	public static void main(String[] args) throws Exception {

		final SslContext sslCtx;
		if (SSL) {
			sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
			System.out.println("SSL build");
		} else {
			sslCtx = null;
			System.out.println("SSLctx = null");
		}

		System.out.println("host ip를 입력하세요.");
		host = sc.nextLine();
		System.out.println("port를 입력하세요.");
		port = sc.nextInt();
		EventLoopGroup group = new NioEventLoopGroup();

		try {
			Bootstrap b = new Bootstrap();
			// 이벤트 루프그룹 설정(서버랑 다르게 1개만 - 연결된 채널이 하나만 존재), 채널의 종류설정(NIO 소켈채널로 설정)
			b.group(group).channel(NioSocketChannel.class)
					// 채널의 초기화 방법 설정
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline p = ch.pipeline();
							if (sslCtx != null) {
								p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
								System.out.println("클라이언트SSLhandler");
							}
							p.addLast(new EchoClientHandler());
						}
					});
			/*
			 * connect() : 메서드의 호출결과로 비동기 메서드의 처리 결과 확인, ChannelFuture 객체의 sync 메서드는
			 * ChannelFuture 객체의 요청이 완료될때가지 대기, 단, 요청이 실패하면 예외를 던진다.
			 */

			ChannelFuture f = b.connect("localhost", 8889).sync(); // 4. 비동기 입출력 메소드 connect 호출

			f.channel().closeFuture().sync();
		} finally {
			System.out.println("셧다운..");
			group.shutdownGracefully();

		}

	}

}
