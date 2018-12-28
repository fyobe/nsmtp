package de.m3y3r.nstmp.command;

import de.m3y3r.nstmp.model.MailTransaction;
import de.m3y3r.nstmp.model.SessionContext;
import de.m3y3r.nstmp.model.SmtpCommandReply;
import de.m3y3r.nstmp.model.SmtpReplyStatus;
import io.netty.channel.ChannelHandlerContext;

public class Mail implements SmtpCommand {

	@Override
	public CharSequence getCommandWord() {
		return "MAIL";
	}

	@Override
	public SmtpCommandReply processCommand(SessionContext ctxMailSession, ChannelHandlerContext ctxChannel,
			CharSequence argument) {
		MailTransaction mailTx = new MailTransaction();
		mailTx.setFrom(argument);

		ctxMailSession.mailTransaction = mailTx;
		SmtpCommandReply reply = new SmtpCommandReply(SmtpReplyStatus.R250, "OK");
		return reply;
	}

}