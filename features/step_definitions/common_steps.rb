Given(/^I am logged in as a (CSR|CSM) user$/) do |role|
  use_admin_user(role)
end

Given(/^I am a logged in user without admin permissions$/) do
  use_admin_user_without_permissions
end

Given(/^I am a logged out user$/) do
  use_logged_out_admin_user
end


Given(/^a malformed customer id (.*)$/) do |malformed_id|
  use_malformed_user_id(malformed_id)
end