<%--

    Copyright (C) 2012 Esup Portail http://www.esup-portail.org
    Copyright (C) 2012 UNR RUNN http://www.unr-runn.fr
    Copyright (C) 2012 RECIA http://www.recia.fr
    @Author (C) 2012 Vincent Bonamy <Vincent.Bonamy@univ-rouen.fr>
    @Contributor (C) 2012 Jean-Pierre Tran <Jean-Pierre.Tran@univ-rouen.fr>
    @Contributor (C) 2012 Julien Marchal <Julien.Marchal@univ-nancy2.fr>
    @Contributor (C) 2012 Julien Gribonvald <Julien.Gribonvald@recia.fr>
    @Contributor (C) 2012 David Clarke <david.clarke@anu.edu.au>
    @Contributor (C) 2012 BULL http://www.bull.fr
    @Contributor (C) 2012 Pierre Bouvret <pierre.bouvret@u-bordeaux4.fr>
    @Contributor (C) 2012 Franck Bordinat <franck.bordinat@univ-jfc.fr>

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>

<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<portlet:defineObjects />

<c:set var="n">
  <portlet:namespace />
</c:set>

<div class="portlet-title">
  <h2>
    <spring:message code="error.title" />
  </h2>
</div>

<div class="portlet-section">

  <div class="portlet-section-body">
    <p>
      <spring:message code="error.description" />
    </p>
  </div>

</div>

