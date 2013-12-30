package com.aperture.docx.template;

public class QuestionParser implements NotificationService.Notifier{
	// constant

	public static interface FinishUpCallback {
		// simple
		void apply(String context);
	}

	FinishUpCallback callback;
	int anchor = 0;
	StringBuilder buffer = new StringBuilder();

	public QuestionParser(StringBuilder pureText, int index, FinishUpCallback cb) {
		// register callback
		callback = cb;

		// start
		int begin = index - Constant.QUESTION_BLANK_LENGTH - Constant.QUESTION_CONTEXT_RADIUS;
		begin = begin >= 0 ? begin : 0;

		buffer.append(pureText.substring(begin));
		anchor = index - begin;
		
		buffer.replace( anchor - Constant.QUESTION_BLANK_LENGTH, anchor, "<_Answer_>");
	}

	public void tail(Object obj) {
		if (obj instanceof org.docx4j.wml.Text){
			String text = ((org.docx4j.wml.Text) obj).getValue();
			buffer.append(text);

			if (buffer.length() - anchor >= Constant.QUESTION_CONTEXT_RADIUS) {
				if (callback != null) {
					callback.apply(buffer.substring(0, anchor
							+ Constant.QUESTION_CONTEXT_RADIUS));

					callback = null;
				}
			}
		}
	}
	
	public void forceTailing(){
		if (callback != null){
			callback.apply(buffer.toString());
			
			callback = null;
		}
	}
}
