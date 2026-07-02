package com.example.archat.controller;

import jakarta.servlet.http.HttpServlet;

public abstract class BaseController extends HttpServlet {
    protected static final String VIEW_PREFIX = "/WEB-INF/views";
}
