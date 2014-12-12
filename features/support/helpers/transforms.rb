Transform(/^Goodwill.*|Credit Promotions|Credit Refund$/)do | reason |
  reason_enum = reason.gsub(/[() ]/, '(' => '', ')' => '', ' ' => '') # removing brackets and spaces
  reason_enum
end
