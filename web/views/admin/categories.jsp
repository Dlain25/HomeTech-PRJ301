<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="model.Category"%>

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
    List<Category> categories =
        (List<Category>) request.getAttribute("categories");

    String path = request.getContextPath();
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>HomeTech - Category Management</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f6f9;
            padding: 30px;
        }
        h1 { color: #173b66; }
        table {
            width: 100%;
            border-collapse: collapse;
            background: white;
        }
        th, td {
            padding: 12px;
            border: 1px solid #ddd;
            text-align: left;
        }
        th {
            background: #173b66;
            color: white;
        }
        input, textarea, select {
            padding: 8px;
            margin: 5px;
        }
        button {
            padding: 8px 15px;
            cursor: pointer;
        }
        .form-box {
            background: white;
            padding: 20px;
            margin-bottom: 25px;
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


<h1>HomeTech - Category Management</h1>

<div class="form-box">
    <h2>Add New Category</h2>

    <form method="post" action="<%= path %>/admin/categories">
        <input type="hidden" name="action" value="add">

        <input type="text" name="categoryName"
               placeholder="Category name"
               maxlength="255" required>

        <input type="text" name="description"
               placeholder="Description">

        <select name="active">
            <option value="true">Active</option>
            <option value="false">Inactive</option>
        </select>

        <button type="submit">Add Category</button>
    </form>
</div>

<h2>Category List</h2>

<table>
    <tr>
        <th>ID</th>
        <th>Category Name</th>
        <th>Description</th>
        <th>Status</th>
        <th>Actions</th>
    </tr>

    <% if (categories != null) {
        for (Category c : categories) { %>

    <tr>
        <td><%= c.getCategoryId() %></td>

        <td><%= esc(c.getCategoryName()) %></td>

        <td><%= esc(c.getDescription()) %></td>

        <td><%= c.isActive() ? "Active" : "Inactive" %></td>

        <td>
            <form method="post"
                  action="<%= path %>/admin/categories">

                <input type="hidden" name="action" value="update">

                <input type="hidden" name="categoryId"
                       value="<%= c.getCategoryId() %>">

                <input type="text" name="categoryName"
                       value="<%= esc(c.getCategoryName()) %>"
                       maxlength="255" required>

                <input type="text" name="description"
                       value="<%= esc(c.getDescription()) %>">

                <select name="active">
                    <option value="true"
                        <%= c.isActive() ? "selected" : "" %>>
                        Active
                    </option>
                    <option value="false"
                        <%= !c.isActive() ? "selected" : "" %>>
                        Inactive
                    </option>
                </select>

                <button type="submit">Update</button>
            </form>

            <form method="post"
                  action="<%= path %>/admin/categories">

                <input type="hidden" name="action" value="delete">

                <input type="hidden" name="categoryId"
                       value="<%= c.getCategoryId() %>">

                <button type="submit">Deactivate</button>
            </form>
        </td>
    </tr>

    <%  }
       } %>
</table>

</body>
</html>