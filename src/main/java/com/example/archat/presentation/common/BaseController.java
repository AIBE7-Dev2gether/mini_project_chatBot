package com.example.archat.presentation.common;

import jakarta.servlet.http.HttpServlet;

public abstract class BaseController extends HttpServlet {
    protected static final String VIEW_PREFIX = "/WEB-INF/views";
}
