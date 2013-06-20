package jp.kddilabs.tsm.android;

public class StringTools {
	public final static StringBuilder inbuffer_join(StringBuilder b,
			final String token, final String... strings) {
		int last_token_1index = strings.length - 1;

		// empty 'strings' case
		if (last_token_1index == -1)
			return b;

		// adds a string and a token, like "bla,"
		for (int i = 0; i < last_token_1index; i++)
			b.append(strings[i]).append(token);
		// adds the final ending string
		b.append(strings[last_token_1index]);

		return b;
	}

	public final static String join(final String token, final String... strings) {
		if (strings.length == 0)
			return "";
		StringBuilder b = new StringBuilder();
		return inbuffer_join(b, token, strings).toString();
	}

	public final static String join_footer(final String token,
			final String footer, String... strings) {
		if (strings.length == 0)
			return new String(footer);
		StringBuilder b = new StringBuilder();
		return inbuffer_join(b, token, strings).append(footer).toString();
	}

	public final static String join_header(final String token,
			final String header, String... strings) {
		if (strings.length == 0)
			return new String(header);
		StringBuilder b = new StringBuilder();
		b.append(header);
		return inbuffer_join(b, token, strings).toString();
	}

	public final static String join_header_footer(final String token,
			final String header, final String footer, final String... strings) {
		if (strings.length == 0) {
			StringBuilder b = new StringBuilder();
			return b.append(header).append(token).append(footer).toString();
		}
		StringBuilder b = new StringBuilder();
		b.append(header);
		return inbuffer_join(b, token, strings).append(footer).toString();
	}
}
