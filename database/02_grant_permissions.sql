USE HomeTechDB;
SET XACT_ABORT ON;
-- Replace with an EXISTING admin username. This script never promotes a customer.
DECLARE @username VARCHAR(60) = 'REPLACE_WITH_ADMIN_USERNAME';
DECLARE @userId INT;
SELECT @userId=u.user_id FROM dbo.Users u JOIN dbo.Roles r ON r.role_id=u.role_id
WHERE u.username=@username AND r.role_name='ADMIN' AND u.status='ACTIVE';
IF @userId IS NULL THROW 51010, N'Choose an existing active ADMIN username.', 1;
-- Remove unwanted rows from this VALUES list for a restricted admin.
INSERT dbo.AdminPermissions(user_id, permission_code)
SELECT @userId, p.code FROM (VALUES
 ('ORDER_REVIEW'),('PAYMENT_REVIEW'),('TOPUP_REVIEW'),
 ('SERVICE_REVIEW'),('RETURN_REVIEW'),('REPORT_VIEW')) p(code)
WHERE NOT EXISTS (SELECT 1 FROM dbo.AdminPermissions a
    WHERE a.user_id=@userId AND a.permission_code=p.code);
SELECT * FROM dbo.AdminPermissions WHERE user_id=@userId;
