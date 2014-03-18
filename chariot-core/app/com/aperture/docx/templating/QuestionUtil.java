package com.aperture.docx.templating;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;

import com.aperture.docx.core.Docx;

public class QuestionUtil {
	// question resolver stuff
	
	interface Resolver {
		void resolve(org.docx4j.wml.Text qt);
	}

	static void resolveQuestion(Docx doc, Object cstart, Resolver resolver) {
		Object next = doc.getNextObject(cstart);
		if (next instanceof R) {
			R r = (R) next;
			if (r.getContent().size() > 0) {
				Object o  = (XmlUtils
						.unwrap(r.getContent().get(0)));
				if ( o instanceof org.docx4j.wml.Text ){
					org.docx4j.wml.Text qt = (org.docx4j.wml.Text)o;

					if (qt.getValue().matches("^__+$")) {
						Object end = doc.getNextObject(next);

						if (end instanceof CommentRangeEnd) {
							// final this is surely a question
							resolver.resolve(qt);
						}
					}	
				}
			}
		}
	}

	static public org.docx4j.wml.Text getQuestionText(Docx doc, Object cstart) {
		final List<org.docx4j.wml.Text> container = new ArrayList<org.docx4j.wml.Text>();
		resolveQuestion(doc, cstart, new Resolver() {

			@Override
			public void resolve(Text qt) {
				container.add(qt);
			}
		});
		return container.size() > 0 ? container.get(0) : null;
	}

	static public int getQuestionBlankSize(Docx doc, Object cstart) {
		org.docx4j.wml.Text qt = getQuestionText(doc, cstart);

		return qt != null ? qt.getValue().length() : -1;
	}

	static public boolean isQuestionMarker(Docx doc, Object cstart) {
		return getQuestionBlankSize(doc, cstart) > 0;
	}

	static void fillBlank(Text question, String answer) {
		org.docx4j.wml.R container = (org.docx4j.wml.R) question.getParent();
		container.getContent().clear();

		Text at = Docx.factory.createText();
		at.setValue(answer);

		container.getContent().add(at);
	}

	static public void fillQuestionBlank(Docx d, Map<String, String> answers) {
		final Docx doc = d;
		final Map<String, Text> targetMap = new HashMap<String, Text>();
		new TraversalUtil(doc.getBody(), new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				//
				if (o instanceof CommentRangeStart) {
					org.docx4j.wml.Text text = QuestionUtil.getQuestionText(
							doc, o);
					if (text != null) {
						// is surely question;
						String questionId = doc
								.getCommentTextById(((CommentRangeStart) o)
										.getId());

						targetMap.put(questionId, text);
					}
				}
				return null;
			}

		});

		for (Map.Entry<String, Text> entry : targetMap.entrySet()) {
			if (answers.containsKey(entry.getKey())) {
				String answer = answers.get(entry.getKey());
				QuestionUtil.fillBlank(entry.getValue(), answer);
			}
		}
	}
}
