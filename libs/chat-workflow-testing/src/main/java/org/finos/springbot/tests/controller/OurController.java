package org.finos.springbot.tests.controller;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.finos.springbot.workflow.annotations.ChatButton;
import org.finos.springbot.workflow.annotations.ChatRequest;
import org.finos.springbot.workflow.annotations.ChatVariable;
import org.finos.springbot.workflow.annotations.WorkMode;
import org.finos.springbot.workflow.content.Addressable;
import org.finos.springbot.workflow.content.Chat;
import org.finos.springbot.workflow.content.CodeBlock;
import org.finos.springbot.workflow.content.Message;
import org.finos.springbot.workflow.content.Table;
import org.finos.springbot.workflow.content.Tag;
import org.finos.springbot.workflow.content.User;
import org.finos.springbot.workflow.content.Word;
import org.finos.springbot.workflow.form.Button;
import org.finos.springbot.workflow.form.Button.Type;
import org.finos.springbot.workflow.form.ButtonList;
import org.finos.springbot.workflow.form.FormSubmission;
import org.finos.springbot.workflow.response.AttachmentResponse;
import org.finos.springbot.workflow.response.MessageResponse;
import org.finos.springbot.workflow.response.WorkResponse;
import org.springframework.stereotype.Controller;

@Controller
public class OurController {
	
	public static final String SOME_ROOM = "The Room Where It Happened"; 
	
	public List<Object> lastArguments;
	public String lastMethod;
	
	// todo: do we need any other kinds of wildcards?
	@ChatRequest(value = "*", addToHelp = false) 
	public void listenToEverything(Message m) {
		// guarav's reminder bot should do this - it needs to parse the date out of every
		// message
		lastArguments = Collections.singletonList(m);
		lastMethod = "listenToEverything";
	}
	
	@ChatButton(value = Person.class, showWhen = WorkMode.VIEW, buttonText = "call")
	public Collection<TestObject> callPerson(Person arg) {
		// do your own form processing
		lastArguments = Collections.singletonList(arg);
		lastMethod = "callPerson";
		return Arrays.asList(new TestObject());
	}
	
	@ChatButton(value = StartClaim.class, buttonText = "start", rooms={"The Room Where It Happened"})
	public TestObject startNewClaim(StartClaim sc) {
		// can't run without StartClaim, returns form to begin a process..
		// user fills it in and this runs.
		lastArguments = Collections.singletonList(sc);
		lastMethod = "startNewClaim";
		return new TestObject();
	}
	

	@ChatButton(value = FormSubmission.class, showWhen = WorkMode.EDIT, buttonText = "go")
	public void processForm(FormSubmission f) {
		// do your own form processing
		// is this needed?
		lastArguments = Collections.singletonList(f);
		lastMethod = "processForm";
	}
	
	
	@ChatRequest(value="list", rooms= {SOME_ROOM}) 
	public void doCommand(Message m) {
		// do something when user types in "/list"
		lastArguments = Collections.singletonList(m);
		lastMethod = "doCommand";
	}

	
	@ChatRequest("show {user}") 
	public void userDetails(@ChatVariable("user") User u) {
		// provide some user details, e.g. /show @Rob Moffat
		lastArguments = Collections.singletonList(u);
		lastMethod = "userDetails";
	}
	
	@ChatRequest("userDetails2 {user}") 
	public void userDetails2(@ChatVariable("user") User u, User author) {
		// provide some user details, e.g. /show @Rob Moffat, also provides the person who typed the command
		lastArguments = Arrays.asList(u, author);
		lastMethod = "userDetails2";
	}

	@ChatRequest("process-table {sometable} {user}") 
	public void process1(@ChatVariable("sometable") Table t, @ChatVariable(required = false, value="user") User u) {
		// provide some processing for a table.
		lastArguments = Arrays.asList(t, u);
		lastMethod = "process-table";
	}

	@ChatRequest("update {code}") 
	public void process2(@ChatVariable("code") CodeBlock cb) {
		// provide some processing for a block of code
		lastArguments = Collections.singletonList(cb);
		lastMethod = "process2";
	}
	
	@ChatRequest({
		"add {user} to {hashtag}", 
		"add {user} {hashtag}"}) 
	public void addUserToTopic(@ChatVariable("user") User u, @ChatVariable("hashtag") Tag t) {
		// provide some processing for a block of code
		lastArguments = Arrays.asList(u, t);
		lastMethod = "addUserToTopic";
	}
	
	
	@ChatRequest(admin = true, value = "delete {user}")
	public void removeUserFromRoom(@ChatVariable("user") User u, Chat r) {
		lastArguments = Arrays.asList(u, r);
		lastMethod = "removeUserFromRoom";	
	}
	
	@ChatRequest(value="ban {word}")
	public MessageResponse banWord(@ChatVariable("word") Word w, Addressable a) {
		lastArguments = Collections.singletonList(w);
		lastMethod = "banWord";
		return new MessageResponse(a, Message.of("banned words: "+w.getText()));
	}
	
	@ChatRequest(value="attachment")
	public AttachmentResponse attachment(Addressable a) {
		lastArguments = Collections.emptyList();
		lastMethod = "attachment";
		String payload = "payload";
		return new AttachmentResponse(a, payload.getBytes(), "somefile", "txt");
	}
	
	@ChatRequest(description="Do blah with a form", value="form1")
	public WorkResponse form1(Addressable a) {
		ButtonList bl = new ButtonList();	
		bl.add(new Button("go", Type.ACTION, "Do The Thing"));
		return new WorkResponse(a, new TestObject(), WorkMode.EDIT, bl, null);
	}
	
	@ChatRequest(value="form2")
	public TestObject form2(Addressable a) {
		return new TestObject();
	}
	
	@ChatButton(value = Person.class, buttonText = "ok")
	public TestObject ok(Person to) {
		return null;
	}
	
	@ChatRequest(value="throwsError")
	public TestObject throwsError() {
		throw new RuntimeException("Error123");
	}
	
	@ChatRequest(value="optionals {thing} {user} {lastword}")
	public void doList(@ChatVariable("thing") List<Word> word, @ChatVariable("user") Optional<User> u, @ChatVariable(value="lastword", required = false) Word w) {
		lastArguments = Arrays.asList(word, u, w);
		lastMethod = "doList";
	}

}
