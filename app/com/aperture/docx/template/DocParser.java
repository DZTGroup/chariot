package com.aperture.docx.template;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docx4j.TraversalUtil;
import org.docx4j.openpackaging.exceptions.Docx4JException;

// aperture science
import com.aperture.docx.Docx;
import com.aperture.docx.template.model.Block;
//import com.aperture.docx.template.model.Node;
import com.aperture.docx.template.model.Question;

public class DocParser {
	Docx doc;
	final List<Question> questions = new ArrayList<Question>();
	final Block moduleTreeRoot = new Block(null);

	public DocParser(Docx d) {
		doc = d;
	}

	NotificationService watcher = new NotificationService();

	protected void parseImpl(Object part) throws Docx4JException {
		// for reading text
		final StringBuilder pureText = new StringBuilder();

		new TraversalUtil(part, new TraversalUtil.CallbackImpl() {
			@Override
			public List<Object> apply(Object o) {
				
				for (NotificationService.Notifier n : watcher.get()) {
					n.tail(o);
				}
				
				if (o instanceof org.docx4j.wml.Text) {
					String text = "";
					final org.docx4j.wml.Text node = (org.docx4j.wml.Text) o;
					text = node.getValue();

					pureText.append(text);
					
					// test quest
					Pattern pattern = Pattern.compile("__<([^<>]+)>__");
					Matcher m = pattern.matcher(pureText);
					int end = -1;
					String qid = null;
					while(m.find()){
						// store the last match
						end = m.end();
						qid = m.group(1);
					}
					// new text match question
					if (qid != null && end > pureText.length() - text.length()){
						final String id = watcher.getId();
						watcher.watch(id, new QuestionParser(pureText, end,
								new QuestionParser.FinishUpCallback() {

									@Override
									public void apply(String context) {
										Question q = new Question();
										q.context = context;
										q.textNode = node;

										questions.add(q);

										watcher.unwatch(id);
									}
								}));
					}

				}

				return null;
			}
		});
	}

	public List<Question> parse() throws Docx4JException {
		this.parseImpl(doc.getBody());
		// end of document
		for (NotificationService.Notifier p : watcher.get()) {
			p.forceTailing();
		}

		return questions;
	}
}
