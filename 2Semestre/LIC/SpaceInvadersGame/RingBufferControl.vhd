library ieee;
use ieee.std_logic_1164.all;

entity RingBufferControl is
    port(
    DAV : in std_logic;
    CTS : in std_logic;
    full : in std_logic;
    empty : in std_logic;
	 clk : in std_logic;
	 RESET : in std_logic;
	 Wr : out std_logic;
	 SELpg : out std_logic;
	 Wreg : out std_logic;
    DAC : out std_logic;
	 incPut : out std_logic;
	 incGet : out std_logic
    );
end RingBufferControl;

architecture behavioral of RingBufferControl is 
type STATE_TYPE is (STATE_I, STATE_IM, STATE_IF, STATE_M, STATE_MM, STATE_MF, STATE_F);

signal CURRENT_STATE, NEXT_STATE : STATE_TYPE;

begin
CURRENT_STATE<= STATE_I when RESET='1' else NEXT_STATE when rising_edge(clk);

GENERATENEXTSTATE:
process (CURRENT_STATE, DAV, CTS , full, empty)
	begin
	case CURRENT_STATE is
		when STATE_I => if (DAV = '1' and full = '0') then
							 NEXT_STATE <= STATE_IM;
							 elsif (DAV ='0' and CTS='1' and empty = '0') then
							 NEXT_STATE <= STATE_MF;
							 elsif (DAV ='0' and CTS='1' and empty = '1') then
							 NEXT_STATE <= STATE_I;
							 elsif (DAV ='0' and CTS='0') then
							 NEXT_STATE <= STATE_I;
							 elsif (DAV = '1' and full = '1' and CTS = '0') then
							 NEXT_STATE <= STATE_I;
							 elsif (DAV = '1' and full = '1' and CTS = '1') then
							 NEXT_STATE <= STATE_MF;
							 end if;
		when STATE_IM => NEXT_STATE <= STATE_IF;
		when STATE_IF => NEXT_STATE <= STATE_M;
		when STATE_M => NEXT_STATE <= STATE_MM;
		when STATE_MM => if (DAV = '1') then
							NEXT_STATE <= STATE_MM;
							else
							NEXT_STATE <= STATE_I;
							end if;
		when STATE_MF => if (CTS = '1') then
							NEXT_STATE <= STATE_MF;
							else
							NEXT_STATE <= STATE_F;
							end if;					
		when STATE_F => NEXT_STATE <= STATE_I;
		
		end case;
end process;    
SELpg <= '1' when ((CURRENT_STATE = STATE_IM) or (CURRENT_STATE = STATE_IF))
			else '0';
Wr <= '1' when ((CURRENT_STATE = STATE_IF))
			else '0';
incPut <= '1' when ((CURRENT_STATE = STATE_M))
			else '0';
DAC <= '1' when ((CURRENT_STATE = STATE_MM))
			else '0';
Wreg <= '1' when ((CURRENT_STATE = STATE_MF))
			else '0';
incGet <= '1' when ((CURRENT_STATE = STATE_F))
			else '0';		
end behavioral;