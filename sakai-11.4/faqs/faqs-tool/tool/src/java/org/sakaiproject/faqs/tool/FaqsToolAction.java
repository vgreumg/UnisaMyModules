/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.faqs.tool;

import java.sql.Timestamp;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.cheftool.api.Menu;
import org.sakaiproject.cheftool.menu.MenuDivider;
import org.sakaiproject.cheftool.menu.MenuEntry;
import org.sakaiproject.cheftool.menu.MenuImpl;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.faqs.cover.FaqsService;
import org.sakaiproject.faqs.dataModel.FaqCategory;
import org.sakaiproject.faqs.dataModel.FaqContent;
import org.sakaiproject.faqs.dataModel.FaqsListForm;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;

/**
 * <p>
 * FaqsToolAction is the faqs display tool
 * </p>
 */
public class FaqsToolAction extends VelocityPortletPaneledAction {
	private static final long serialVersionUID = 1L;
	private static Logger M_log = LoggerFactory.getLogger(FaqsToolAction.class);
	private static ResourceLoader rb = new ResourceLoader("faqs");
	@Getter
	@Setter
	private boolean addFaqContent;
	@Getter
	@Setter
	private boolean editFaqContent;
	@Getter
	@Setter
	private boolean deleteFaqContent;
	@Getter
	@Setter
	private boolean addFaqCategory;
	@Getter
	@Setter
	private boolean editFaqCategory;
	@Getter
	@Setter
	private boolean deleteFaqCategory;
	protected static final String expandedCatogories = "";
	protected static final String STATE_DISPLAY_MODE = "display_mode";
	private EventTrackingService eventTrackingService = null;

	/**
	 * Populate the state object, if needed.
	 */
	/*protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData rundata) {

	}
*/
	@SuppressWarnings("deprecation")
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state) {
		context.put("tlang", rb);
		context.put(Menu.CONTEXT_ACTION, state.getAttribute(STATE_ACTION));
		String userId = SessionManager.getCurrentSessionUserId();
		String siteReference = null;
		String siteTitle = "";
		String siteId = ToolManager.getCurrentPlacement().getContext();
		String template = (String) getContext(rundata).get("template");

		try {
			siteReference = getSiteReference(siteId);
		} catch (IdUnusedException e) {
			context.put("nopermission", rb.getString("faqs.permissionerror"));
			return template + "_noaccess";
		}
		System.out.println("template " + template);
		M_log.info(this + "faqs name " + template);


		context.put("addFaqContent", SecurityService.unlock("faqs.add", siteReference));
		context.put("editFaqContent", SecurityService.unlock("faqs.contentedit", siteReference));
		context.put("deleteFaqContent", SecurityService.unlock("faqs.contentdelete", siteReference));
		context.put("addFaqCategory", SecurityService.unlock("faqs.createcategory", siteReference));
		context.put("editFaqCategory", SecurityService.unlock("faqs.editcatergory", siteReference));
		context.put("deleteFaqCategory", SecurityService.unlock("faqs.categorydelete", siteReference));

	

		try {
			if (userId != null) {
				if (userId.equalsIgnoreCase("admin")) {
					context.put("userType", "Instructor");
				} else {
					context.put("userType",
							UserDirectoryService.getUser(SessionManager.getCurrentSessionUserId()).getType());
				}
			} else {
				// log
				return (String) getContext(rundata).get("faqs") + "_noaccess";
			}
		} catch (UserNotDefinedException idu) {
			// log
			return (String) getContext(rundata).get("faqs") + "_noaccess";
		}

		try {
			context.put("siteTitle", SiteService.getSite(siteId).getTitle().toString());
		} catch (IdUnusedException e) {
			System.out.println(e.getLocalizedMessage());
		}

		if ("EDIT_CATEGORY".equals(state.getAttribute(STATE_DISPLAY_MODE))) {
			//context.put("systemDate", new Timestamp(new Date().getTime()));
			return template + "_edit_category";
		}
		
		FaqsListForm listForm = new FaqsListForm();
		List categories = FaqsService.getFaqCategories(siteId);

		Vector v2 = (Vector) state.getAttribute(expandedCatogories);
		if (v2 != null) {
			// listForm.setExpandedCategories(expandCategory(rundata, context));
			Vector v = (Vector) state.getAttribute(expandedCatogories);
			listForm.setExpandedCategories(v);
		}

		if (listForm.getExpandedCategories() == null) {
			listForm.setExpandedCategories(new Vector());
		}
		boolean faqExist = listForm.isFaqExist();
		int countExpandedCategories = 0;
		Iterator i = categories.iterator();

		FaqCategory c = new FaqCategory();
		while (i.hasNext()) {
			c = (FaqCategory) i.next();
			// FaqContent cont = FaqContent
			Vector ids = listForm.getExpandedCategories();
			Iterator idsi = ids.iterator();

			while (idsi.hasNext()) {
				Integer id = (Integer) idsi.next();
				if (id.equals(c.getCategoryId())) {

					c.setExpanded(true);
					List contents = FaqsService.getFaqContents(c.getCategoryId());

					if (contents.size() > 0) {
						faqExist = faqExist | ((contents.size() > 0) && c.isExpanded() == true);
						if (c.isExpanded()) {
							countExpandedCategories++;
						}

					} else {
						faqExist = faqExist && (countExpandedCategories > 1);
					}
					c.setContents(contents);
					context.put("contents", c.getContents());
					continue;
				}
			}
		}

		listForm.setFaqExist(faqExist);
		context.put("faqExist", faqExist);
		listForm.setCategories(categories);
		context.put("categories", categories);
		
		Menu bar = new MenuImpl();
		bar.add(new MenuEntry(rb.getString("link.addfaq"), "addFaq"));
		bar.add(new MenuEntry(rb.getString("link.createcategory"), "createCategory"));
		context.put(Menu.CONTEXT_MENU, bar);
		
		if ("ADD_CATEGORY".equals(state.getAttribute(STATE_DISPLAY_MODE))) {
			context.put("systemDate", new Timestamp(new Date().getTime()));
			return template + "_add_category";
		}
		if ("ADD_FAQ".equals(state.getAttribute(STATE_DISPLAY_MODE))) {
			context.put("systemDate", new Timestamp(new Date().getTime()));
			
			return template + "_add_faq";
		}
		context.put("service", FaqsService.getInstance());
		return template + "_main_view";

	}

	public static final String getSiteReference(String siteId) throws IdUnusedException {
		return SiteService.getSite(siteId).getReference(); // returns /site/site_id
	}

	public Vector expandCategory(RunData rundata, Context context) {
		Vector ids = null;
		FaqsListForm listForm = new FaqsListForm();

		int itemReference = Integer.parseInt(rundata.getParameters().getString("itemReference"));
		String peid = ((JetspeedRunData) rundata).getJs_peid();
		SessionState state = ((JetspeedRunData) rundata).getPortletSessionState(peid);

		listForm.setExpandCategoryId(itemReference);

		if (listForm.getExpandedCategories() == null) {
			listForm.setExpandedCategories(new Vector());
		}
		ids = listForm.getExpandedCategories();
		Iterator i = ids.iterator();

		while (i.hasNext()) {
			Integer id = (Integer) i.next();
			if (id.equals(itemReference)) {
				break;
			}
		}
		ids.add(listForm.getExpandCategoryId());
		listForm.setExpandedCategories(ids);
		state.setAttribute(expandedCatogories, ids);

		return ids;
	}
	public Vector collapseCategory(RunData rundata, Context context) {
		Vector ids = null;
		FaqsListForm listForm = new FaqsListForm();

		int itemReference = Integer.parseInt(rundata.getParameters().getString("itemReference"));
		String peid = ((JetspeedRunData) rundata).getJs_peid();
		SessionState state = ((JetspeedRunData) rundata).getPortletSessionState(peid);

		listForm.setExpandCategoryId(itemReference);
		listForm.setFaqExist(false);

		if (listForm.getExpandedCategories() == null) {
			listForm.setExpandedCategories(new Vector());
		}
		
		ids = (Vector) state.getAttribute("expandedCatogories");
	     if(ids == null) {
	    	 ids = new Vector();
	     }
		//ids = listForm.getExpandedCategories();
		Iterator i = ids.iterator();

		while (i.hasNext()) {
			Integer id = (Integer) i.next();
			if (id.equals(itemReference)) {
				ids.remove(id);
			}
		}		
		listForm.setExpandedCategories(ids);
		state.setAttribute(expandedCatogories, ids);

		return ids;
	}


	
	public void createCategory(RunData data, Context context) {
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(STATE_DISPLAY_MODE, "ADD_CATEGORY");
	}

	public void addFaq(RunData data, Context context) {
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(STATE_DISPLAY_MODE, "ADD_FAQ");
	}
	
	public void doCancel(RunData data, Context context) {
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute(STATE_DISPLAY_MODE, null);
	}
	
	public void saveCategory(RunData data, Context context) { 
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);
		state.setAttribute(STATE_DISPLAY_MODE, null);
       String categoryDesc = data.getParameters().getString("categoryDesc").trim();
     
       if(categoryDesc==null || categoryDesc.length() < 2) {
		addAlert(state, rb.getString("faq.alert.nocategorydesc"));
		state.setAttribute(STATE_DISPLAY_MODE, "ADD_CATEGORY");		
       }
       //save category to db
       FaqsService.insertFaqCategory(ToolManager.getCurrentPlacement().getContext(), categoryDesc);
       
       if (eventTrackingService == null)
		{
			eventTrackingService = (EventTrackingService) ComponentManager.get("org.sakaiproject.event.api.EventTrackingService");
		}
       eventTrackingService.post(eventTrackingService.newEvent("faqs.categoryadd",ToolManager.getCurrentPlacement().getContext(), false));
	}
	
 
	
	public void updateCategory(RunData data, Context context) { 
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);
		state.setAttribute(STATE_DISPLAY_MODE, null);
       String categoryDesc = data.getParameters().getString("categoryDesc").trim();
     
       if(categoryDesc==null || categoryDesc.length() < 1) {
		addAlert(state, rb.getString("faq.alert.nocategorydesc"));
		state.setAttribute(STATE_DISPLAY_MODE, "EDIT_CATEGORY");	 
       }
       //save category to db
       //FaqsService.insertFaqCategory(ToolManager.getCurrentPlacement().getContext(), categoryDesc);
       
       if (eventTrackingService == null)
		{
			eventTrackingService = (EventTrackingService) ComponentManager.get("org.sakaiproject.event.api.EventTrackingService");
		}
       eventTrackingService.post(eventTrackingService.newEvent("faqs.categoryedit",ToolManager.getCurrentPlacement().getContext(), false));
	}
	
	public void editcategory(RunData rundata, Context context) { 
		String peid = ((JetspeedRunData) rundata).getJs_peid();
		SessionState state = ((JetspeedRunData) rundata).getPortletSessionState(peid);
		int itemReference = Integer.parseInt(rundata.getParameters().getString("itemReference").trim());
		//get category for category id
		List category = FaqsService.getFaqCategory(itemReference);
		context.put("category", category);

		state.setAttribute(STATE_DISPLAY_MODE, "EDIT_CATEGORY");		
	}
	
	
	public void editFaqContent(RunData rundata, Context context) { 
		String peid = ((JetspeedRunData) rundata).getJs_peid();
		SessionState state = ((JetspeedRunData) rundata).getPortletSessionState(peid);
		int faqContentId = Integer.parseInt(rundata.getParameters().getString("itemReference").trim());
		//get category for category id
		List faqContent = FaqsService.getFaqContent(faqContentId);
		context.put("content", faqContent);

		state.setAttribute(STATE_DISPLAY_MODE, "EDIT_FAQ_CONTENT");		
	}
	
	public void saveContent(RunData data, Context context) { 
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		state.setAttribute(STATE_DISPLAY_MODE, null);
	   FaqContent  faqContent = new FaqContent();
       String selectedCategory = data.getParameters().getString("selectedCategory").trim();
       String newCategoryDesc = data.getParameters().getString("newCategoryDesc").trim();
       String question = data.getParameters().getString("question").trim();
       String answer = data.getParameters().getString("answer").trim();
       context.put("selectedCategory", selectedCategory);
       context.put("newCategoryDesc",newCategoryDesc);
       context.put("question", question);
       context.put("answer",answer);

          
       if((selectedCategory.equals("-1")) && (newCategoryDesc.length() < 1)) {
    	addAlert(state, rb.getString("faq.content.alert.nocategorydesc"));
    	state.setAttribute(STATE_DISPLAY_MODE, "ADD_FAQ");
       }
       
       if((selectedCategory != "-1") && (newCategoryDesc.length() > 0)) {
       	addAlert(state, rb.getString("faq.content.alert.selectedboth"));
       	state.setAttribute(STATE_DISPLAY_MODE, "ADD_FAQ");
       }
       
       if(question==null || question.length() < 1) {
   		addAlert(state, rb.getString("faq.alert.noquestiontitle"));
   		state.setAttribute(STATE_DISPLAY_MODE, "ADD_FAQ");		
      }
 
       
     
	}
}