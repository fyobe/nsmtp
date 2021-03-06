package de.m3y3r.nsmtp.model;

public class SmtpReplyStatus {

	public static final SmtpReplyStatus R220 = new SmtpReplyStatus((short) 220);
	public static final SmtpReplyStatus R221 = new SmtpReplyStatus((short) 221);
	public static final SmtpReplyStatus R250 = new SmtpReplyStatus((short) 250);
	
	public static final SmtpReplyStatus R354 = new SmtpReplyStatus((short) 354);

	public static final SmtpReplyStatus R450 = new SmtpReplyStatus((short) 450);
	public static final SmtpReplyStatus R454 = new SmtpReplyStatus((short) 454);

	public static final SmtpReplyStatus R500 = new SmtpReplyStatus((short) 500);
	public static final SmtpReplyStatus R501 = new SmtpReplyStatus((short) 501);
	public static final SmtpReplyStatus R502 = new SmtpReplyStatus((short) 502);

	private short status;

	public SmtpReplyStatus(short status) {
		this.setStatus(status);
	}

	public short getStatus() {
		return status;
	}

	public void setStatus(short status) {
		//TODO: check for valid status codes
		this.status = status;
	}
}
