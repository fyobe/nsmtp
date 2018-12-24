package de.m3y3r.nstmp.handler.codec.smtp;

import de.m3y3r.nstmp.handler.codec.smtp.model.SmtpCommandReply;
import de.m3y3r.nstmp.handler.codec.smtp.model.SmtpReplyStatus;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * RFC2821 SMTP server - "3.1 Session Initiation"
 *
 * https://tools.ietf.org/html/rfc2821
 * @author thomas
 *
 */
public class SessionInitiationHandler extends ChannelInboundHandlerAdapter {

	/** 
	 * This can wait up to 5 minutes according to spec to wait for the system load to hit a certain level
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		SmtpCommandReply reply = new SmtpCommandReply(SmtpReplyStatus.R220, Config.INSTANCE.getDomain());
		ctx.writeAndFlush(reply);
	}

}
