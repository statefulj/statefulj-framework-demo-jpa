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

import org.statefulj.framework.core.annotations.StatefulController;
import org.statefulj.framework.core.annotations.Transition;
import org.statefulj.webapp.model.Account;
import org.statefulj.webapp.model.Notification;
import org.statefulj.webapp.model.User;

import static org.statefulj.webapp.model.Notification.*;
import static org.statefulj.webapp.controller.NotificationController.*;

@StatefulController(
	clazz=Notification.class,
	startState=NON_EXISTENT,
	noops={
		@Transition(from=SHOWING, event=DELETE_EVENT, to=DELETED)
	}
)
public class NotificationController {
	
	// EVENTS
	//
	public static final String NOTIFY_EVENT = "notify";
	public static final String DELETE_EVENT = "jersey:delete:/notifications/{id}";
	
	@Transition(from=NON_EXISTENT, event=NOTIFY_EVENT, to=SHOWING)
	public void createNotification(Notification notification, String event, User user, Account account, String msg) {
		notification.setType(account.getState().toLowerCase());
		notification.setMessage(msg);
		user.addNotification(notification);
	}
}
