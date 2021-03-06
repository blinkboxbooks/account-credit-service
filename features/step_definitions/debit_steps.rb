Given(/^I (?:try to )?debit the customer ([\d.-]+) in GBP( using the same requestId as before)?$/) do |amount_debit, use_last_request_id|
  request_id = use_last_request_id ? last_request_id : new_request_id
  post_admin_account_debit(last_admin_user.access_token, amount_debit, user_id_of(last_public_user), request_id)
end
