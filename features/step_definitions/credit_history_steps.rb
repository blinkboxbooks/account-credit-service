require 'jsonpath'

@expected_credit_history

Given(/^a user has the following credit history:$/) do | table |
  @expected_credit_history = table.hashes
  new_public_user
  table.hashes.each do | event |
    event_type = event['event_type']
    if event_type == 'credit'
      post_admin_account_credit(use_admin_user('csm').access_token, event['amount'], event['reason'],
                                user_id_of(last_public_user), request_id)
    elsif event_type == 'debit'
      post_admin_account_debit(use_csm_user.access_token, event['amount'], user_id_of(last_public_user),
                               request_id)
    else
      fail 'Did not recognise the event: ' + event_type
    end
  end
end

Given(/^a user with credit history$/) do
  # creating new user with default credit history
  post_admin_account_credit(use_admin_user('csm').access_token, '9.99', 'Credit Refund',
                            user_id_of(new_public_user), request_id)
end

When(/^I request for the user's credit history$/) do
  get_admin_account_credit(last_admin_user.access_token, user_id_of(last_public_user))
end

Then(/^the credit history contains the above events$/) do
  pending
  response_hash = parse_response_data
  @expected_credit_history.reverse.each_with_index do | event, index | # expecting credit history response to order events by desc timestamp
    # checking common fields
    expect(response_hash['items'][index]['amount']['amount'].to_f).to eq(event['amount'].to_f)
    expect(response_hash['items'][index]['amount']['currency']).to eq('GBP')
    expect(Time.parse(response_hash['items'][index]['dateTime'])).to be_truthy

    event_type = event['event_type']
    if event_type == 'credit'
      expect(response_hash['items'][index]['type']).to eq('credit')
      expect(response_hash['items'][index]['reason']).to eq(event['reason'])
      expect(response_hash['items'][index]['issuer']['name']).to be_truthy
      expect(response_hash['items'][index]['issuer']['roles'][0]).to be_truthy
    elsif event_type == 'debit'
      expect(response_hash['items'][index]['type']).to eq('debit')
    else
      fail 'Unrecognised event_type: ' + event_type
    end
  end
end

Then(/^the event items in the history response (does not )?contains? the following attributes:$/) do | does_not_contain, table |
  jsonpath_list = table.transpose.raw[0]
  jsonpath_list.each do | path |
    path_to_field = JsonPath.new(path)
    if does_not_contain
      expect(path_to_field.on(parse_response_data.to_s)).to be_empty
    else
      expect(path_to_field.on(parse_response_data.to_s)).not_to be_empty
    end
  end
end
