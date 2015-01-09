require 'jsonpath'

Given(/^a customer has the following credit history:$/) do |table|
  @expected_credit_history = table.hashes
  new_public_user
  table.hashes.each do | event |
    event_type = event['event_type']
    if event_type == 'credit'
      post_admin_account_credit(use_admin_user('csm').access_token, event['amount'], event['reason'],
                                user_id_of(last_public_user), new_request_id)
    elsif event_type == 'debit'
      post_admin_account_debit(use_csm_user.access_token, event['amount'], user_id_of(last_public_user),
                               new_request_id)
    else
      fail 'Did not recognise the event: ' + event_type
    end
  end
end

Given(/^a customer with credit history$/) do
  # creating new user with default credit history
  post_admin_account_credit(use_admin_user('csm').access_token, '9.99', 'GoodwillServiceIssue',
                            user_id_of(new_public_user), new_request_id)
end

When(/^I request for the user's credit history$/) do
  get_admin_account_credit(last_admin_user.access_token, user_id_of(last_public_user))
end

Then(/^the credit history contains the above events$/) do
  response_hash = parse_last_api_response
  @expected_credit_history = @expected_credit_history.reverse
  @expected_credit_history.each_with_index do |event, index| # expecting credit history response to order events by desc timestamp
    # checking common fields
    expect(response_hash['items'][index]['amount']['value'].to_f).to eq(event['amount'].to_f)
    expect(response_hash['items'][index]['amount']['currency']).to eq('GBP')
    expect(Time.parse(response_hash['items'][index]['dateTime'])).to be_truthy

    event_type = event['event_type']
    if event_type == 'credit'
      expect(response_hash['items'][index]['type']).to eq('credit')
      expect(response_hash['items'][index]['reason']).to eq(event['reason'])
      expect(response_hash['items'][index]['issuer']['name']).to be_truthy
      #expect(response_hash['items'][index]['issuer']['roles'][0]).to be_truthy # not yet implemented CRED-60
    elsif event_type == 'debit'
      expect(response_hash['items'][index]['type']).to eq('debit')
    else
      fail 'Unrecognised event_type: ' + event_type
    end
  end
end

Then(/^the event items in the history response (does not )?contains? the following attributes:$/) do |does_not_contain, table|
  attr_list = table.transpose.raw[0]
  attr_list.each do | attr |
    field_exists?(attr, !does_not_contain)
  end
end

def field_exists?(field_name, exists)
  if field_name == 'issuer'
    path_to_field = JsonPath.new('$items[*].issuer.name')
    jsonpath_exists?(path_to_field, exists)

    path_to_field = JsonPath.new('$items[*].issuer.roles[*]')
    jsonpath_exists?(path_to_field, exists)
  elsif field_name == 'amount'
    path_to_field = JsonPath.new('$items[*].amount.currency')
    jsonpath_exists?(path_to_field, exists)

    path_to_field = JsonPath.new('$items[*].amount.value')
    jsonpath_exists?(path_to_field, exists)
  else
    path_to_field = JsonPath.new("$items[*].#{field_name}")
    jsonpath_exists?(path_to_field, exists)
  end
end

def jsonpath_exists?(path_to_field, expected_is_present)
  expect(!path_to_field.on(last_response_as_string).empty?).to eq(expected_is_present)
end
