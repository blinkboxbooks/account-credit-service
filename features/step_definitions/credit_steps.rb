Given(/^a user with £(\d+(?:\.)?(?:\d*)) credit balance$/) do | credit_balance |
  user = new_public_user

  if credit_balance.to_i > 0
    post_admin_account_credit(use_csr_user.access_token, credit_balance, 'Credit Promotions', user_id_of(user), request_id)
  end
end

When(/^I credit the user (-?\d+(?:\.)?(?:\d*)) in GBP with the reason: (.*)$/) do | credit_amount, reason |
  post_admin_account_credit(last_admin_user.access_token, credit_amount, reason,
                            user_id_of(last_public_user), request_id)
end

When(/^I try to credit the user (\d+(?:\.)?(?:\d*)) in GBP using the same requestId as before$/) do | credit_amount |
  post_admin_account_credit(last_admin_user.access_token, credit_amount, 'Credit Promotions',
                            user_id_of(last_public_user), last_request_id)
end

Then(/^the user has(?: overall)? credit balance of (\d+(?:\.)?(?:\d*)) in GBP$/) do | expected_credit |
  get_admin_account_credit(last_admin_user.access_token, user_id_of(last_public_user))

  expect(parse_response_data['balance']['amount']).to eq(expected_credit.to_i)
  expect(parse_response_data['balance']['currency']).to eq('GBP')
end

Then(/^the amount (?:credited|debited) is (\d+(?:\.)?(?:\d*)) in GBP$/) do | amount_credited |
  expect(parse_response_data['amount']['value']).to eq(amount_credited.to_i)
  expect(parse_response_data['amount']['currency']).to eq('GBP')
end
