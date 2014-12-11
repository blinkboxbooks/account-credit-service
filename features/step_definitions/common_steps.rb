Given(/^I am logged in as an? (CSR|CSM) user$/) do | role |
    use_admin_user(role)
end

Given(/^an unknown user$/) do
  use_unknown_user
end

Given(/^I am a logged in user without admin permissions$/) do
  use_admin_user_without_permissions
end

Given(/^I am a logged out user$/) do
  use_logged_out_admin_user
end
