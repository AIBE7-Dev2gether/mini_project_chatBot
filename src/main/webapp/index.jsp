<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // 루트 경로(/) 접속 시, 자동으로 /chat 컨트롤러로 이동시킵니다.
    // AuthFilter가 작동하여, 미인증 상태라면 다시 /login으로 안전하게 넘겨줍니다.
    response.sendRedirect(request.getContextPath() + "/chat");
%>
