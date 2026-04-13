library ieee;
use ieee.std_logic_1164.all;

entity DEC_1_2 is
port(
	a0: in std_logic;
	e: in std_logic;
	o0: out std_logic;
	o1: out std_logic
	);
end DEC_1_2;

architecture structural of DEC_1_2 is
begin
o0 <= e and not a0;
o1 <= e and a0;
end structural;