package de.m3y3r.nsmtp.handler.codec.smtp;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.m3y3r.nsmtp.command.SmtpCommand;
import de.m3y3r.nsmtp.command.SmtpRegistry;
import de.m3y3r.nsmtp.model.SessionContext;
import de.m3y3r.nsmtp.model.SmtpCommandReply;
import de.m3y3r.nsmtp.model.SmtpReplyStatus;
import de.m3y3r.nsmtp.util.CharSequenceComparator;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Attribute;

/**
 * rfc5321 SMTP server
 *
 * https://tools.ietf.org/html/rfc5321
 * @author thomas
 *
 */
public class SmtpCommandHandler extends ChannelInboundHandlerAdapter {

	private static Logger log = LoggerFactory.getLogger(SmtpCommandHandler.class);

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ByteBuf frame = (ByteBuf) msg;

		if(!isSessionContext(ctx)) {
			/*FIXME: is this state even possible? */

			/* we did receive an command before we send an initial greetings reply */
			/* what to do here? is it possible to send a reply here?
			 */
			return;
		}

		if(frame.readableBytes() < 4) {
			throw new UnknownCommandException("");
		}

		SessionContext sessionContext = ctx.channel().attr(SessionContext.ATTRIBUTE_KEY).get();

		CharSequence line = frame.readCharSequence(frame.readableBytes(), StandardCharsets.US_ASCII);

		int iac = line.length();
		for(int i = 0, n = line.length(); i < n; i++) {
			if(line.charAt(i) == ' ') {
				iac = i;
				break;
			}
		}
		CharSequence cmd = line.subSequence(0, iac);
		CharSequence argument = line.length() > iac ? line.subSequence(iac, line.length()) : null;

		validateCommand(cmd);

		/* order-independent commands:
		 * "The NOOP, HELP, EXPN, VRFY, and RSET commands can be used at any time during a session"
		 */
		validateCommandOrder(sessionContext, cmd);

		/* process command */
		SmtpCommand smtpCmd = SmtpRegistry.INSTANCE.getCommand(cmd.toString());
		if(smtpCmd == null) {
			log.error("Received unknown command {}", cmd);
			// unknown command
			Object reply = new SmtpCommandReply(SmtpReplyStatus.R500, "WAT?");
			ctx.writeAndFlush(reply);
			return;
		}

		SmtpCommandReply reply = smtpCmd.processCommand(sessionContext, ctx, argument);
		if(reply != null) {
			ctx.writeAndFlush(reply);
		}

		sessionContext.lastCmd = cmd;
	}

	/**
	 * was this session started at all?
	 * @param ctx
	 * @return 
	 */
	private boolean isSessionContext(ChannelHandlerContext ctx) {
		Attribute<SessionContext> sessionStarted = ctx.channel().attr(SessionContext.ATTRIBUTE_KEY);
		return sessionStarted.get() != null;
	}

	private static final CharSequence[] ALWAYS_ALLOWED_COMMANDS = new String[] {"HELO", "EHLO", "RSET"};

	/**
	 * is the command allowed in the current state
	 * @param ctx
	 * @param cmd
	 */
	private void validateCommandOrder(SessionContext ctx, CharSequence cmd) {
		if(Arrays.stream(ALWAYS_ALLOWED_COMMANDS).anyMatch(c -> CharSequenceComparator.equals(c, cmd)))
			return;

		if(ctx.lastCmd == null) {
			return;
		}

		// is a mail transaction going on already
		if(ctx.mailTransaction != null) {
			if(
					CharSequenceComparator.equals(cmd, "RCPT") && CharSequenceComparator.equals(ctx.lastCmd, "MAIL") ||
					CharSequenceComparator.equals(cmd, "RCPT") && CharSequenceComparator.equals(ctx.lastCmd, "RCPT") ||
					CharSequenceComparator.equals(cmd, "DATA") && CharSequenceComparator.equals(ctx.lastCmd, "RCPT")
					) {
				return;
			}
			throw new IllegalStateException();
		}
	}

	private static final CharSequence[] VALID_COMMANDS = new String[] {"HELO", "EHLO", "MAIL", "RSET", "VRFY", "EXPN", "NOOP", "QUIT"};

	/**
	 * is this an valid SMTP command?
	 * @param cmd
	 */
	private boolean validateCommand(CharSequence cmd) {
		return Arrays.stream(VALID_COMMANDS).anyMatch(c -> CharSequenceComparator.equals(c, cmd));
	}
}
