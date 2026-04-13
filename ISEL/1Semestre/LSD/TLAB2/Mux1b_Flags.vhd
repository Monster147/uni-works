library ieee;

use ieee.std_logic_1164.all;

entity Mux1b_Flags is
port(
	A_Mux : in std_logic;
	B_Mux : in std_logic;
	S_Mux : in std_logic;
	Y_Mux : out std_logic
	);
end Mux1b_Flags;

architecture Mux1b_Flags_ARCH of Mux1b_Flags is
begin

Y_Mux <= (B_Mux and not S_MUX) or (S_Mux and A_Mux);

end Mux1b_Flags_ARCH;