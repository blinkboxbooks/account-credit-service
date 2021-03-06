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

Then(/^the request fails because the ([\w.-]+) was invalid$/) do |the_invalid|
  to_contain =
    case the_invalid
      when 'currency' then 'unsupported_currency'
    end
  expect(HttpCapture::RESPONSES.last.body.include?(to_contain)).to eq(true)
end