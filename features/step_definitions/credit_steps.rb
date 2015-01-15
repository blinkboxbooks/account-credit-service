Given(/^a customer with ([\d.-]+) in GBP of credit$/) do |credit_balance|
  user = new_public_user
  if credit_balance.to_i > 0
    post_admin_account_credit(use_admin_user('csm').access_token, credit_balance, 'GBP', 'GoodwillServiceIssue', user_id_of(user), new_request_id)
  end
end

When(/^I credit the customer ([\d.-]+) in ([\w.-]+) with the reason: (.*)$/) do |credit_amount, currency, reason|
  post_admin_account_credit(last_admin_user.access_token, credit_amount, currency, reason,
                            user_id_of(last_public_user), new_request_id)
end

When(/^I try to credit the customer ([\d.-]+) in GBP using the same requestId as before$/) do |credit_amount|
  post_admin_account_credit(last_admin_user.access_token, credit_amount, 'GBP', 'GoodwillServiceIssue',
                            user_id_of(last_public_user), last_request_id)
end

Then(/^the customer has(?: an overall)? credit balance of ([\d.-]+) in GBP$/) do |expected_credit|
  get_admin_account_credit(last_admin_user.access_token, user_id_of(last_public_user))
  expect(parse_last_api_response['balance']['value']).to eq(expected_credit.to_f)
  expect(parse_last_api_response['balance']['currency']).to eq('GBP')
end

Then(/^the amount (?:credited|debited) is ([\d.-]+) in GBP$/) do |amount|
  expect(parse_last_api_response['amount']['value']).to eq(amount.to_f)
  expect(parse_last_api_response['amount']['currency']).to eq('GBP')
end

Given(/^(\d+) users with one of the following roles are logged in$/) do |concurrent_users, table|
  roles = table.hashes.map {|hash| hash['role']}
  @cs_users = 1.upto(concurrent_users.to_i).map {use_admin_user(roles.shuffle.first)}
end

When(/^all the users credit the customer ([\d.-]+) in GBP$/) do |amount|
  @concurrent_responses = []
  # Naively Simulate concurrent users.
  # It seems to work OK: DB rows show credits with identical millisecond timestamps.
  threads = @cs_users.map do |user|
    Thread.new {@concurrent_responses << post_admin_account_credit(user.access_token, amount, 'GBP', 'GoodwillServiceIssue',
                                          user_id_of(last_public_user), new_request_id)}
  end
  threads.each {|thr| thr.join}
end

Then(/^all the requests are successful$/) do
  @concurrent_responses.each do |response|
    expect(response.status_code).to eq(204)
  end
end