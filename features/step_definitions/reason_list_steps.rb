When(/^I make a request for the reasons list$/) do
  get_reasons_list
end

Then(/^the reasons returned are:$/) do | table |
  expected_reasons = table.transpose.raw[0]
  actual_reasons = parse_last_api_response['reasons']

  expect(expected_reasons & actual_reasons).to_be expected_reasons
end
