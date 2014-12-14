<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:url var="createSavingsAccountUrl" value="/accounts/savings" />
<c:url var="createCheckingAccountUrl" value="/accounts/checking" />
<c:url var="createLoanAccountUrl" value="/accounts/loan" />
<c:url var="deleteUserUrl" value="/user/delete" />
<html>
	<head>
		<title>User Page</title>
	</head>
	<body>
		<script>
			$(document).ready(function() {

				var token = $("meta[name='_csrf']").attr("content");
				var header = $("meta[name='_csrf_header']").attr("content");
				$(document).ajaxSend(function(e, xhr, options) {
				  xhr.setRequestHeader(header, token);
				});
				  
		        $('.alert-dismissable button.close').click(function() {
					$.ajax({
						url : '/ajax/notifications/' + $(this).attr('notification'),
						type : 'DELETE'
					});
				});
			});
		</script>
		<div class="user-header row">
			<div class="col-md-offset-1 col-md-10">
				<h2 class="pull-left">${user.firstName} ${user.lastName}</h2>
				<a href="${deleteUserUrl}" class="pull-right btn btn-default">Delete User</a>
			</div>
		</div>
		<div class="row">
			<div class="col-md-offset-1 col-md-10">
				<c:if test="${not empty user.accounts}">
					<div class="notifications">
						<c:forEach items="${user.notifications}" var="notification">
							<c:if test="${notification.type.equals('rejected')}">
								<c:set var="alertType" value="alert-warning"/>
							</c:if>
							<c:if test="${not notification.type.equals('rejected')}">
								<c:set var="alertType" value="alert-info"/>
							</c:if>
							<div class="alert alert-dismissable ${alertType}">
								<button notification="${notification.id}" type="button" class="close" data-dismiss="alert">x</button>
								${notification.message}
							</div>
						</c:forEach>
					</div>
				</c:if>
				<h3>Accounts:</h3>
				<c:if test="${empty user.accounts}">
					<div class="alert alert-info">
						You need to set up some accounts
					</div>
				</c:if>
				<c:if test="${not empty user.accounts}">
					<table class="table table-striped table-hover ">
						<tr>
							<th>Type</th>
							<th>State</th>
							<th>Amount</th>
						</tr>
						<c:forEach items="${user.accounts}" var="account">
						<tr>
							<td>${account.type}</td>
							<td>${account.state}</td>
							<td>${account.amount}</td>
						</tr>
						</c:forEach>
					</table>
				</c:if>
				<div class="account-create">
					<a href="${createSavingsAccountUrl}" class="btn btn-default">+ Savings Account</a>
					<a href="${createCheckingAccountUrl}" class="btn btn-default">+ Checking Account</a>
					<a href="${createLoanAccountUrl}" class="btn btn-default">+ Loan Account</a>
				</div>
			</div>
		</div>
	</body>
</html>
