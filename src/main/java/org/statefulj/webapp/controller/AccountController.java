/***
 * 
 * Copyright 2014 Andrew Hall
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.statefulj.webapp.controller;

import javax.annotation.Resource;

import org.apache.camel.Produce;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.servlet.ModelAndView;
import org.statefulj.framework.core.annotations.StatefulController;
import org.statefulj.framework.core.annotations.Transition;
import org.statefulj.framework.core.annotations.Transitions;
import org.statefulj.webapp.form.AccountForm;
import org.statefulj.webapp.messaging.AccountApplication;
import org.statefulj.webapp.messaging.AccountApplicationReviewer;
import org.statefulj.webapp.model.Account;

import static org.statefulj.webapp.model.Account.*;
import static org.statefulj.webapp.rules.AccountRules.*;

import org.statefulj.webapp.services.AccountService;
import org.statefulj.webapp.services.NotificationService;

@StatefulController(
	clazz=Account.class,
	startState=NON_EXISTENT,
	factoryId="accountService",
	noops={
		@Transition(from=APPROVAL_PENDING, event=AccountController.ACCOUNT_APPROVED_EVENT, to=ACTIVE),
		@Transition(from=APPROVAL_PENDING, event=AccountController.ACCOUNT_REJECTED_EVENT, to=REJECTED)
	}
)
public class AccountController {
	
	// Events
	//
	static final String ACCOUNT_APPROVED_EVENT = "camel:" + ACCOUNT_APPROVED;
	static final String ACCOUNT_REJECTED_EVENT = "camel:" + ACCOUNT_REJECTED;
	static final String LOAN_APPROVED_EVENT = "camel:" + LOAN_APPROVED;
	static final String LOAN_REJECTED_EVENT = "camel:" + LOAN_REJECTED;
	static final String ACCOUNT_CREATE_EVENT = "springmvc:post:/accounts";
	static final String ACCOUNT_DISPLAY_EVENT = "springmvc:/accounts/{id}";
	
	@Resource
	AccountService accountService;
	
	@Produce(uri=REVIEW_APPLICATION)
	AccountApplicationReviewer applicationReviewer;
	
	@Resource
	NotificationService notificationService;
	
	@Transition(from=NON_EXISTENT, event=ACCOUNT_CREATE_EVENT, to=APPROVAL_PENDING)
	public String createAccount(Account account, String event, AccountForm form) {
		
		// Save to database prior to emitting events
		//
		account.setAmount(form.getAmount());
		accountService.save(account);
		
		// Submit the Account Application for approval
		//
		AccountApplication application = new AccountApplication();
		application.setAccountId(account.getId()); // Set the Loan Application Id
		application.setType(account.getType());
		
		applicationReviewer.submitForApproval(application);
		
		return "redirect:/user";
	}

	@Transitions({
		@Transition(from=APPROVAL_PENDING, event=LOAN_APPROVED_EVENT, to=ACTIVE),
		@Transition(from=APPROVAL_PENDING, event=LOAN_REJECTED_EVENT, to=REJECTED)
	})
	public void accountReviewed(Account account, String event, AccountApplication msg) {
		notificationService.onNotify(account.getOwner(), account, msg.getReason());
	}
	
	// Make sure that only the owner can access the account
	//
	@Transition(event=ACCOUNT_DISPLAY_EVENT)
	@PreAuthorize("#account.owner.email == principal.username")
	public ModelAndView displayAccount(Account account, String event) {
		ModelAndView mv = new ModelAndView("account");
		mv.addObject("account", account);
		return mv;
	}
}
