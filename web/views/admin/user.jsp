
<%@page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="model.User"%>

<%!
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
%>

<%
    List<User> users = (List<User>) request.getAttribute("users");
    String keyword = (String) request.getAttribute("keyword");
    String path = request.getContextPath();
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>HomeTech - User Management</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f6f9;
            padding: 30px;
        }
        h1 { color: #173b66; }
        .search-box {
            background: white;
            padding: 20px;
            margin-bottom: 25px;
        }
        input, select {
            padding: 8px;
            margin: 4px;
        }
        button {
            padding: 8px 12px;
            cursor: pointer;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            background: white;
        }
        th, td {
            padding: 10px;
            border: 1px solid #ddd;
            text-align: left;
        }
        th {
            background: #173b66;
            color: white;
        }
        .actions form {
            margin-bottom: 8px;
        }
    </style>
</head>
<body>

<div style="display:flex;
            gap:12px;
            flex-wrap:wrap;
            margin-bottom:25px;">

    <a href="${pageContext.request.contextPath}/admin/dashboard"
       style="background:#173b66;
              color:white;
              padding:11px 18px;
              border-radius:7px;
              text-decoration:none;">
        Dashboard
    </a>

    <a href="${pageContext.request.contextPath}/home"
       style="background:#16804d;
              color:white;
              padding:11px 18px;
              border-radius:7px;
              text-decoration:none;">
        Trang chủ HomeTech
    </a>

</div>

<h1>HomeTech - User Management</h1>

<div class="search-box">
    <form method="get" action="<%= path %>/admin/users">
        <input type="text" name="keyword"
               placeholder="Search username, email, full name"
               value="<%= esc(keyword) %>">
        <button type="submit">Search</button>
        <a href="<%= path %>/admin/users">Show All</a>
    </form>
</div>

<table>
    <thead>
        <tr>
            <th>ID</th>
            <th>Username</th>
            <th>Full Name</th>
            <th>Email</th>
            <th>Phone</th>
            <th>Role</th>
            <th>Status</th>
            <th>Created At</th>
            <th>Actions</th>
        </tr>
    </thead>

    <tbody>
    <% if (users != null && !users.isEmpty()) {
        for (User u : users) { %>

        <tr>
            <td><%= u.getUserId() %></td>
            <td><%= esc(u.getUsername()) %></td>
            <td><%= esc(u.getFullName()) %></td>
            <td><%= esc(u.getEmail()) %></td>
            <td><%= esc(u.getPhone()) %></td>
            <td><%= esc(u.getRoleName()) %></td>
            <td><%= esc(u.getStatus()) %></td>
            <td><%= u.getCreatedAt() == null
                    ? "" : u.getCreatedAt() %></td>
            <td class="actions">

                <form method="post"
                      action="<%= path %>/admin/users">
                    <input type="hidden" name="action" value="role">
                    <input type="hidden" name="userId"
                           value="<%= u.getUserId() %>">

                    <select name="roleId">
                        <option value="1"
                            <%= u.getRoleId() == 1 ? "selected" : "" %>>
                            ADMIN
                        </option>
                        <option value="2"
                            <%= u.getRoleId() == 2 ? "selected" : "" %>>
                            CUSTOMER
                        </option>
                    </select>

                    <button type="submit">Update Role</button>
                </form>

                <form method="post"
                      action="<%= path %>/admin/users">
                    <input type="hidden" name="action" value="status">
                    <input type="hidden" name="userId"
                           value="<%= u.getUserId() %>">

                    <select name="status">
                        <option value="ACTIVE"
                            <%= "ACTIVE".equals(u.getStatus())
                                    ? "selected" : "" %>>
                            ACTIVE
                        </option>
                        <option value="LOCKED"
                            <%= "LOCKED".equals(u.getStatus())
                                    ? "selected" : "" %>>
                            LOCKED
                        </option>
                    </select>

                    <button type="submit">Update Status</button>
                </form>

            </td>
        </tr>

    <%  }
       } else { %>

        <tr>
            <td colspan="9" style="text-align:center;">
                No users found.
            </td>
        </tr>

    <% } %>
    </tbody>
</table>

</body>
</html>
