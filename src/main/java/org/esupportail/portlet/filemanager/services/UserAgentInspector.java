/**
 * Copyright (C) 2012 Esup Portail http://www.esup-portail.org
 * Copyright (C) 2012 UNR RUNN http://www.unr-runn.fr
 * Copyright (C) 2012 RECIA http://www.recia.fr
 * @Author (C) 2012 Vincent Bonamy <Vincent.Bonamy@univ-rouen.fr>
 * @Contributor (C) 2012 Jean-Pierre Tran <Jean-Pierre.Tran@univ-rouen.fr>
 * @Contributor (C) 2012 Julien Marchal <Julien.Marchal@univ-nancy2.fr>
 * @Contributor (C) 2012 Julien Gribonvald <Julien.Gribonvald@recia.fr>
 * @Contributor (C) 2012 David Clarke <david.clarke@anu.edu.au>
 * @Contributor (C) 2012 BULL http://www.bull.fr
 * @Contributor (C) 2012 Pierre Bouvret <pierre.bouvret@u-bordeaux4.fr>
 * @Contributor (C) 2012 Franck Bordinat <franck.bordinat@univ-jfc.fr>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.esupportail.portlet.filemanager.services;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.portlet.PortletRequest;

import org.springframework.beans.factory.InitializingBean;

public class UserAgentInspector implements InitializingBean {
    
    private List<String> userAgentMobile;
    
    private final List<Pattern> patterns = new ArrayList<Pattern>();
    
    public void setUserAgentMobile(List<String> userAgentMobile) {
		this.userAgentMobile = userAgentMobile;
	}

	public void afterPropertiesSet() {
        // Compile our patterns
        for (String userAgent : userAgentMobile) {
            patterns.add(Pattern.compile(userAgent));
        }
    }

    public boolean isMobile(PortletRequest req) {
        
    	boolean isMobile = false;
    	
        // Assertions.
        if (req == null) {
            String msg = "Argument 'req' cannot be null";
            throw new IllegalArgumentException(msg);
        }
        
        String userAgent = req.getProperty("user-agent");
        if (userAgent != null && patterns.size() != 0) {
            for (Pattern pattern : patterns) {
                if (pattern.matcher(userAgent).matches()) {
                	isMobile = true;
                    break;
                }
            }
        }

        return isMobile;

    }

}
